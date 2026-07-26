package com.offerlens.data

import android.content.Context
import timber.log.Timber
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.ProductDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.widget.Toast

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// DataStore Extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_prefs")

@Singleton
class PremiumRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PurchasesUpdatedListener {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val PREMIUIM_KEY = booleanPreferencesKey("is_premium")
    private val DEBUG_PREMIUM_KEY = booleanPreferencesKey("debug_premium_override")

    /**
     * Debug-only sticky premium grant.
     *
     * The debug bypass in launchPurchaseFlow() sets premium directly, but
     * checkPurchases() runs afterwards - from the billing connection and again from the
     * Firestore user-doc listener - finds no real Play purchase, and would immediately
     * set it back to false. The grant appeared to work and then silently vanished a
     * moment later, taking the Smart Wallet toggle with it. This flag makes the debug
     * grant survive those checks. It is never set in release builds.
     */
    private var debugPremiumOverride = false

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // SKU Parameter (In-App Product ID from Play Console)
    companion object {
        const val PRODUCT_ID_PREMIUM = "offerlens_premium_lifetime"
    }

    init {
        // Load initial state from cache
        scope.launch {
            val prefs = context.dataStore.data.first()
            if (com.offerlens.BuildConfig.DEBUG) {
                debugPremiumOverride = prefs[DEBUG_PREMIUM_KEY] ?: false
            }
            val cachedPremium = (prefs[PREMIUIM_KEY] ?: false) || debugPremiumOverride
            _isPremium.value = cachedPremium

            // Then check online
            startBillingConnection()
            observeManualPremiumStatus()
        }
    }

    private var premiumListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var lastKnownManualPremium: Boolean? = null

    private fun observeManualPremiumStatus() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // Remove previous listener if exists
            premiumListenerRegistration?.remove()
            premiumListenerRegistration = null
            lastKnownManualPremium = null

            if (user == null) {
                // Signed out (e.g. "Delete My Data") - don't leave a stale premium
                // status cached for whichever account signs in next.
                updatePremiumStatus(false)
            }

            if (user != null) {
                premiumListenerRegistration = firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Timber.e(e, "Listen failed")
                            return@addSnapshotListener
                        }

                        val isManualPremium = snapshot?.getBoolean("isPremium") ?: false
                        // Only react when the field actually changed - avoids an avoidable
                        // Billing queryPurchasesAsync() call on every unrelated user-doc write.
                        if (isManualPremium == lastKnownManualPremium) return@addSnapshotListener
                        lastKnownManualPremium = isManualPremium

                        if (isManualPremium) {
                            Timber.d("Manual Premium Granted via Firestore")
                            updatePremiumStatus(true)
                        } else {
                            // If not manually granted, fall back to billing check
                            checkPurchases()
                        }
                    }
            }
        }
    }

    private fun startBillingConnection() {
        if (billingClient.isReady) return
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing Connected")
                    checkPurchases()
                    fetchProductDetails()
                } else {
                    Timber.e("Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.d("Billing Disconnected")
                // Retry logic could go here, but we rely on ensureBillingConnection() for critical actions
            }
        })
    }

    suspend fun ensureBillingConnection(): Boolean {
        if (billingClient.isReady) return true
        
        startBillingConnection()
        
        // Wait up to 5 seconds for connection
        var attempts = 0
        while (!billingClient.isReady && attempts < 10) {
            delay(500)
            attempts++
        }
        
        return billingClient.isReady
    }

    private fun fetchProductDetails() {
        scope.launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_PREMIUM)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            val result = billingClient.queryProductDetails(params)
            
            if (result.productDetailsList?.isNotEmpty() == true) {
                _productDetails.value = result.productDetailsList!![0]
            }
        }
    }


    private fun checkPurchases() {
        if (!billingClient.isReady) return

        billingClient.queryPurchasesAsync(
            com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.i("User Canceled Purchase")
        } else {
            Timber.e("Purchase Error: ${billingResult.debugMessage}")
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        var isPremiumActive = false
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Check if it's our product
                if (purchase.products.contains(PRODUCT_ID_PREMIUM)) {
                    isPremiumActive = true
                    
                    // Acknowledge if needed
                    if (!purchase.isAcknowledged) {
                        val params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(params) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Timber.d("Purchase Acknowledged")
                            }
                        }
                    }
                    
                    // Sync to Firestore (Backup)
                    syncPremiumToFirestore()
                }
            }
        }

        updatePremiumStatus(isPremiumActive)
    }

    private fun syncPremiumToFirestore() {
        // [BLOCKER RESOLVED] Client-side sync disabled because Security Rules correctly
        // prevent users from modification of their own 'isPremium' field for security.
        // Premium status is managed via Play Billing or manual Admin action.
        /*
        val user = auth.currentUser ?: return
        val data = hashMapOf("isPremium" to true, "lastSynced" to System.currentTimeMillis())
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { Timber.d("Premium synced to Firestore") }
            .addOnFailureListener { e -> Timber.e(e, "Failed to sync premium") }
        */
        Timber.d("Premium sync skipped (Security Constraint)")
    }


    private fun updatePremiumStatus(status: Boolean) {
        // A debug grant wins over a negative billing result, so the periodic purchase
        // checks can't revoke it mid-session.
        val effective = status || debugPremiumOverride
        _isPremium.value = effective
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[PREMIUIM_KEY] = effective
            }
        }
    }

    // Public method to launch purchase flow
    suspend fun launchPurchaseFlow(activity: android.app.Activity) {
        // DEBUG BYPASS: Instantly grant premium in Debug builds
        if (com.offerlens.BuildConfig.DEBUG) {
            Timber.d("DEBUG MODE: Granting premium instantly without Play Store.")
            debugPremiumOverride = true
            scope.launch {
                context.dataStore.edit { prefs -> prefs[DEBUG_PREMIUM_KEY] = true }
            }
            updatePremiumStatus(true)
            syncPremiumToFirestore()
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    activity,
                    "🛠️ DEBUG: Premium Activated! 💎",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return // StateFlow update auto-triggers Compose UI recomposition — no recreate needed
        }

        if (!ensureBillingConnection()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, "Cannot connect to Google Play Store", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Use cached product details if available, otherwise fetch
        val productDetails = _productDetails.value ?: run {
             // Try fetching again synchronously-ish
             val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_PREMIUM)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            val result = billingClient.queryProductDetails(params)
            val details = result.productDetailsList?.firstOrNull()
            if (details != null) {
                _productDetails.value = details
            }
            details
        }

        if (productDetails != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            
            withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        } else {
             withContext(Dispatchers.Main) {
                Toast.makeText(activity, "Product not found", Toast.LENGTH_SHORT).show()
            }
            Timber.e("Product Details not found for $PRODUCT_ID_PREMIUM")
        }
    }

    /** Outcome of a restore attempt, so the UI can show progress and a definite result. */
    sealed class RestoreResult {
        object Restored : RestoreResult()
        object NothingToRestore : RestoreResult()
        data class Failed(val message: String) : RestoreResult()
    }

    /**
     * Suspends until the Billing query completes and returns what happened.
     *
     * Previously this fired queryPurchasesAsync and returned immediately, so the caller
     * had no way to know when it finished - the button showed no progress and users on a
     * slow connection would tap it repeatedly. It also called activity.recreate() on
     * success, rebuilding the whole screen when the isPremium StateFlow already drives
     * recomposition (the purchase path deliberately avoids that).
     */
    suspend fun restorePurchases(): RestoreResult {
        if (!ensureBillingConnection()) {
            return RestoreResult.Failed("Cannot connect to Google Play Store")
        }

        return suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { result, purchases ->
                if (!cont.isActive) return@queryPurchasesAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPurchases(purchases)
                    val isPremiumNow = purchases.any {
                        it.products.contains(PRODUCT_ID_PREMIUM) &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                    }
                    cont.resume(
                        if (isPremiumNow) RestoreResult.Restored else RestoreResult.NothingToRestore
                    ) { _, _, _ -> }
                } else {
                    cont.resume(RestoreResult.Failed(result.debugMessage.ifBlank { "Restore failed" })) { _, _, _ -> }
                }
            }
        }
    }
}
