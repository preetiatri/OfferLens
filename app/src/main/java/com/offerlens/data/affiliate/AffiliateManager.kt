package com.offerlens.data.affiliate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.offerlens.data.Offer
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Singleton to manage affiliate link resolution.
 * Registry of providers and centralized logic for opening links.
 */
object AffiliateManager {

    private val providers = CopyOnWriteArrayList<AffiliateProvider>()

    // Global switch to kill switch affiliate links if needed, default false until fetched securely
    var isAffiliateSystemEnabled: Boolean = false 
        private set

    init {
        // Safe failover initialization for Firestore Config
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("config").document("affiliates")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Timber.e(e, "Listen failed.")
                        isAffiliateSystemEnabled = false // Bulletproof Offline/Failure fallback
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        isAffiliateSystemEnabled = snapshot.getBoolean("enabled") ?: false
                        // Priority Parsing could happen here based on snapshot data
                    } else {
                        Timber.d("Config missing, using fallback.")
                        isAffiliateSystemEnabled = false
                    }
                }
        } catch (ex: Exception) {
            // Catches cases where Firebase might not be initialized yet
            Timber.e(ex, "Firebase Init Error")
            isAffiliateSystemEnabled = false
        }
    }

    fun registerProvider(provider: AffiliateProvider) {
        providers.add(provider)
        // Keep sorted by priority (Descending)
        providers.sortWith { a, b -> b.priority.compareTo(a.priority) }
    }

    /**
     * Resolves the final URL to open.
     * Iterates through providers in priority order.
     */
    fun resolveLink(originalUrl: String, subId: String? = null): String {
        if (!isAffiliateSystemEnabled || originalUrl.isBlank()) {
            return originalUrl
        }

        // 1. Check for valid URL
        // Simple check to ensure we don't crash on bad input
        if (!originalUrl.startsWith("http")) return originalUrl

        // 2. Iterate providers
        for (provider in providers) {
            try {
                if (provider.canHandle(originalUrl)) {
                    val affiliateLink = provider.generateAffiliateLink(originalUrl, subId)
                    if (!affiliateLink.isNullOrBlank()) {
                        return affiliateLink
                    }
                }
            } catch (e: Exception) {
                // Log error safely, don't crash, continue to next provider / fallback
                Timber.e(e, "Provider failed")
            }
        }

        // 3. Fallback to original
        return originalUrl
    }

    /**
     * Convenience method to open an offer link with context.
     * Handles Intent creation and error catching.
     */
    fun openOfferLink(context: Context, offer: Offer, useSourceUrl: Boolean = false) {
        val targetUrl = if (useSourceUrl) offer.offerSourceUrl else offer.merchantUrl
        
        if (targetUrl.isBlank()) {
            Toast.makeText(context, "Link not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Resolve Affiliate Link
            // We can usage offer.id as subId for tracking which offer converted
            val finalUrl = resolveLink(targetUrl, subId = offer.id)

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}
