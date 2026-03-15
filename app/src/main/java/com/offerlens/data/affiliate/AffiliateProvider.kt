package com.offerlens.data.affiliate

/**
 * Interface for Affiliate Providers (e.g., CueLinks, vCommission, Amazon Associates).
 * Each provider implements logic to convert a direct URL into an affiliate deep link.
 */
interface AffiliateProvider {
    /**
     * Unique name of the provider (e.g., "CueLinks").
     */
    val providerName: String

    /**
     * Priority of the provider. Higher priority execute first.
     * Default: 0. Direct/Fallback should be lowest.
     */
    val priority: Int

    /**
     * Checks if this provider handles the specific domain/URL.
     */
    fun canHandle(url: String): Boolean

    /**
     * Generates a deep link/affiliate link for the given URL.
     * @param url The original merchant URL.
     * @param subId Optional tracking ID (e.g., user ID or source).
     * @return The affiliate link, or null if generation failed.
     */
    fun generateAffiliateLink(url: String, subId: String? = null): String?
}
