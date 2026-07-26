package com.offerlens.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * One row of a multi-tier offer.
 *
 * Bank offers are frequently bundled: a single listing might give a different cap and
 * minimum spend for domestic flights, international flights, hotels and bus. Flattening
 * that into one discount value forces a choice between overstating the benefit or
 * hiding the terms that actually apply, so each variant is kept as its own tier.
 *
 * All fields need defaults - Firestore requires a no-arg constructor to deserialize.
 */
data class OfferTier(
    val label: String = "",                    // e.g. "International Flights"
    val discountValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,     // cap for this tier
    val minOrderValue: Double? = null,         // minimum spend for this tier
    val note: String = ""                      // e.g. "Rs 1000 per passenger"
)

data class Offer(
    val id: String = "",
    val bankName: String = "",
    val paymentType: String = "", // Credit Card, Debit Card, UPI, Wallet
    val merchant: String = "",
    val discountType: String = "", // Percentage, Flat
    val discountValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val minOrderValue: Double? = null,
    /**
     * Per-product breakdown for bundled offers. Empty for ordinary single-tier offers,
     * in which case the top-level discount fields above are the whole story.
     */
    val tiers: List<OfferTier> = emptyList(),
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val description: String = "",
    val merchantUrl: String = "", // Direct link to merchant (e.g., amazon.in)
    val offerSourceUrl: String = "", // Link to where offer was found (e.g., bank page)
    val dealScore: Int = 0,
    val dealBand: String = "", // Green, Yellow, Red
    val successCount: Int = 0,
    val failCount: Int = 0,
    val category: String = "", // Dining, Travel, Shopping, Entertainment, Groceries, Bill Pay & Recharges
    val couponCode: String = "",
    
    // Enhanced T&C fields
    val termsAndConditions: String = "",
    val termsCompleteness: Int = 0, // 0-100 score indicating T&C completeness
    val termsLastUpdated: Timestamp? = null,
    
    // Metadata fields
    val lastFetchedAt: Timestamp? = null,
    val offerHash: String = "", // Unique hash for deduplication
    val verificationStatus: String = "unverified", // verified, user_verified, admin_verified, unverified
    val sourceType: String = "", // api, scrape_static, scrape_dynamic, user_submission, manual_entry
    val submittedBy: String? = null, // User ID if user-submitted
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
