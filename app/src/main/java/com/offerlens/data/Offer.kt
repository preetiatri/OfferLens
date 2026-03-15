package com.offerlens.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Offer(
    val id: String = "",
    val bankName: String = "",
    val paymentType: String = "", // Credit Card, Debit Card, UPI, Wallet
    val merchant: String = "",
    val discountType: String = "", // Percentage, Flat
    val discountValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val minOrderValue: Double? = null,
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
