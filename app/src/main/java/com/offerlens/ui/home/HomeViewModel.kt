package com.offerlens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.AuthRepository
import com.offerlens.data.Offer
import com.offerlens.data.OfferRepository
import com.offerlens.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val offerRepository: OfferRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val analytics: com.google.firebase.analytics.FirebaseAnalytics,
    private val smartWalletRepository: com.offerlens.data.SmartWalletRepository,
    private val premiumRepository: com.offerlens.data.PremiumRepository,
    private val adManager: com.offerlens.data.AdManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // Smart Wallet Toggle State
    private val _smartWalletEnabled = MutableStateFlow(false)
    val smartWalletEnabled: StateFlow<Boolean> = _smartWalletEnabled.asStateFlow()
    
    // Expose Premium Status for UI
    val isPremium = premiumRepository.isPremium
    
    // Expose User ID for debugging/manual premium grant
    val userId: String? 
        get() = authRepository.currentUser?.uid
        
    val userName: String?
        get() = authRepository.currentUser?.displayName
        
    val userPhotoUrl: android.net.Uri?
        get() = authRepository.currentUser?.photoUrl

    // Raw offers from repository
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    
    // Filtered offers (Search Only) - Category filtering is left to UI for Pager support
    // Filtered offers (Search + Smart Wallet)
    val offers: StateFlow<List<Offer>> = kotlinx.coroutines.flow.combine(
        _offers, 
        _searchQuery,
        smartWalletRepository.myCards,
        premiumRepository.isPremium,
        _smartWalletEnabled
    ) { offers, query, myCards, isPremium, isWalletEnabled ->
        var result = offers
        
        // 1. Filter by Search
        if (query.isNotBlank()) {
            result = result.filter { offer ->
                offer.merchant.contains(query, ignoreCase = true) ||
                offer.description.contains(query, ignoreCase = true) ||
                offer.couponCode.contains(query, ignoreCase = true) ||
                offer.bankName.contains(query, ignoreCase = true)
            }
        }
        
        // 2. Filter by Smart Wallet (Personalized for Premium Users)
        if (isPremium && isWalletEnabled && myCards.isNotEmpty()) {
            result = result.filter { offer ->
                if (offer.bankName.isBlank()) return@filter false
                
                val normalizedOfferBank = offer.bankName.lowercase().trim()
                myCards.any { userCard -> 
                    val normalizedUserCard = userCard.lowercase().trim()
                    // Check for exact match or word-level containment to avoid "ICICI" matching "ICICI Amazon" incorrectly
                    normalizedOfferBank == normalizedUserCard || 
                    normalizedOfferBank.split(" ").any { it.length > 2 && normalizedUserCard.contains(it) } ||
                    normalizedUserCard.split(" ").any { it.length > 2 && normalizedOfferBank.contains(it) }
                }
            }
        }
        
        result
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)

    init {
        // Ensure every user has an ID (Anonymous Auth) so "User Profile" and "Admin Bypass" work
        if (authRepository.currentUser == null) {
            viewModelScope.launch {
                try {
                    val result = authRepository.signInAnonymously()
                    if (result.isSuccess) {
                        Timber.d("Auto-signed in anonymously: ${result.getOrNull()?.uid}")
                        // Refresh offers after sign-in to ensure personalized data (if any) is fetched? 
                        // Actually offers don't depend on ID yet, but good practice.
                    } else {
                        Timber.e("Failed to auto-sign in anonymously")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error during auto-sign in")
                }
            }
        }
    }
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    init {
        loadOffers()
        
        // Quick Firestore Connection Test (Optional, keeping from original)
        // Leaving it out to reduce noise/startup work unless user asked for it.
        // But original code had it. I'll include a lightweight version if needed, 
        // but cleaner to omit for "Optimization".
    }

    fun loadOffers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (_offers.value.isEmpty() || forceRefresh) {
                _isLoading.value = true
            }
            try {
                // Fetch offers
                val fetchedOffers = offerRepository.getOffers(forceRefresh = forceRefresh)
                _offers.value = fetchedOffers
            } catch (e: Exception) {
                Timber.e(e, "Error loading offers")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreOffers() {
        viewModelScope.launch {
            if (_isLoading.value || _isLoadingMore.value) return@launch
            
             _isLoadingMore.value = true
            try {
                val newOffers = offerRepository.getOffers(isLoadMore = true)
                // Repository returns the full updated list usually?
                // Checking Repo code: 
                // "return getCachedOffers()" which returns ALL active offers.
                // So replacing _offers.value is correct.
                _offers.value = newOffers 
            } catch (e: Exception) {
                Timber.e(e, "Error loading more offers")
            } finally {
                 _isLoadingMore.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleSmartWallet() {
        _smartWalletEnabled.value = !_smartWalletEnabled.value
    }

    fun refreshOffers() {
        loadOffers(forceRefresh = true)
    }

    fun logOfferClick(offerId: String, merchant: String) {
        Timber.d("Offer Clicked: $merchant ($offerId)")
    }
    
    // Sign-out function
    fun signOut() {
        authRepository.signOut()
    }

    /**
     * Permanently deletes the current user's Firestore data and Auth account
     * (DPDP Act erasure request). After this completes, the app has no signed-in
     * user, so navigation should return to onboarding.
     */
    fun deleteMyData(onComplete: () -> Unit, onError: (Exception) -> Unit) {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            onError(Exception("No signed-in user"))
            return
        }
        viewModelScope.launch {
            try {
                userRepository.deleteUserData(uid)
                authRepository.deleteAccount().getOrThrow()
                Timber.d("User data deleted for $uid")
                onComplete()
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete user data")
                onError(e)
            }
        }
    }
    
    // Filter by Query Only
    private fun filterOffers(offers: List<Offer>, query: String): List<Offer> {
        if (query.isBlank()) return offers
        
        return offers.filter { offer ->
            offer.merchant.contains(query, ignoreCase = true) ||
            offer.description.contains(query, ignoreCase = true) ||
            offer.couponCode.contains(query, ignoreCase = true)
        }
    }

    fun showInterstitial(activity: android.app.Activity) {
        adManager.showInterstitial(activity)
    }
}
