package com.offerlens.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * Room entity for caching offers locally
 * Enables offline access and reduces Firebase reads
 */
@Entity(
    tableName = "offers",
    indices = [
        androidx.room.Index(value = ["bankName"]),
        androidx.room.Index(value = ["category"]),
        androidx.room.Index(value = ["merchant"]),
        // Composite index for efficient sorting by 'Active' and 'Created Date'
        androidx.room.Index(value = ["isActive", "createdAtSeconds"])
    ]
)
data class OfferEntity(
    @PrimaryKey
    val id: String,
    val bankName: String,
    val paymentType: String,
    val merchant: String,
    val discountType: String,
    val discountValue: Double,
    val maxDiscountAmount: Double?,
    val minOrderValue: Double?,
    val startDateSeconds: Long?,
    val startDateNanos: Int?,
    val endDateSeconds: Long?,
    val endDateNanos: Int?,
    val isActive: Boolean,
    val description: String,
    val merchantUrl: String,
    val offerSourceUrl: String,
    val dealScore: Int,
    val dealBand: String,
    val successCount: Int,
    val failCount: Int,
    val category: String,
    val couponCode: String,
    val termsAndConditions: String,
    val termsCompleteness: Int,
    val termsLastUpdatedSeconds: Long?,
    val termsLastUpdatedNanos: Int?,
    val lastFetchedAtSeconds: Long?,
    val lastFetchedAtNanos: Int?,
    val offerHash: String,
    val verificationStatus: String,
    val sourceType: String,
    val submittedBy: String?,
    val createdAtSeconds: Long?,
    val createdAtNanos: Int?,
    val updatedAtSeconds: Long?,
    val updatedAtNanos: Int?,
    val cachedAt: Long = System.currentTimeMillis() // When this was cached locally
)

/**
 * Extension functions to convert between Offer and OfferEntity
 */
fun com.offerlens.data.Offer.toEntity(): OfferEntity {
    return OfferEntity(
        id = id,
        bankName = bankName,
        paymentType = paymentType,
        merchant = merchant,
        discountType = discountType,
        discountValue = discountValue,
        maxDiscountAmount = maxDiscountAmount,
        minOrderValue = minOrderValue,
        startDateSeconds = startDate?.seconds,
        startDateNanos = startDate?.nanoseconds,
        endDateSeconds = endDate?.seconds,
        endDateNanos = endDate?.nanoseconds,
        isActive = isActive,
        description = description,
        merchantUrl = merchantUrl,
        offerSourceUrl = offerSourceUrl,
        dealScore = dealScore,
        dealBand = dealBand,
        successCount = successCount,
        failCount = failCount,
        category = category,
        couponCode = couponCode,
        termsAndConditions = termsAndConditions,
        termsCompleteness = termsCompleteness,
        termsLastUpdatedSeconds = termsLastUpdated?.seconds,
        termsLastUpdatedNanos = termsLastUpdated?.nanoseconds,
        lastFetchedAtSeconds = lastFetchedAt?.seconds,
        lastFetchedAtNanos = lastFetchedAt?.nanoseconds,
        offerHash = offerHash,
        verificationStatus = verificationStatus,
        sourceType = sourceType,
        submittedBy = submittedBy,
        createdAtSeconds = createdAt?.seconds,
        createdAtNanos = createdAt?.nanoseconds,
        updatedAtSeconds = updatedAt?.seconds,
        updatedAtNanos = updatedAt?.nanoseconds
    )
}

fun OfferEntity.toOffer(): com.offerlens.data.Offer {
    return com.offerlens.data.Offer(
        id = id,
        bankName = bankName,
        paymentType = paymentType,
        merchant = merchant,
        discountType = discountType,
        discountValue = discountValue,
        maxDiscountAmount = maxDiscountAmount,
        minOrderValue = minOrderValue,
        startDate = if (startDateSeconds != null && startDateNanos != null) 
            Timestamp(startDateSeconds, startDateNanos) else null,
        endDate = if (endDateSeconds != null && endDateNanos != null) 
            Timestamp(endDateSeconds, endDateNanos) else null,
        isActive = isActive,
        description = description,
        merchantUrl = merchantUrl,
        offerSourceUrl = offerSourceUrl,
        dealScore = dealScore,
        dealBand = dealBand,
        successCount = successCount,
        failCount = failCount,
        category = category,
        couponCode = couponCode,
        termsAndConditions = termsAndConditions,
        termsCompleteness = termsCompleteness,
        termsLastUpdated = if (termsLastUpdatedSeconds != null && termsLastUpdatedNanos != null) 
            Timestamp(termsLastUpdatedSeconds, termsLastUpdatedNanos) else null,
        lastFetchedAt = if (lastFetchedAtSeconds != null && lastFetchedAtNanos != null) 
            Timestamp(lastFetchedAtSeconds, lastFetchedAtNanos) else null,
        offerHash = offerHash,
        verificationStatus = verificationStatus,
        sourceType = sourceType,
        submittedBy = submittedBy,
        createdAt = if (createdAtSeconds != null && createdAtNanos != null) 
            Timestamp(createdAtSeconds, createdAtNanos) else null,
        updatedAt = if (updatedAtSeconds != null && updatedAtNanos != null) 
            Timestamp(updatedAtSeconds, updatedAtNanos) else null
    )
}
