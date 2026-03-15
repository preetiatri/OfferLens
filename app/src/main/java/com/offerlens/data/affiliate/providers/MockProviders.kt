package com.offerlens.data.affiliate.providers

import com.offerlens.data.affiliate.AffiliateProvider

/**
 * A mock provider for testing (or generic fallback logic).
 * Currently set as low priority.
 */
class DirectProvider : AffiliateProvider {
    override val providerName: String = "Direct"
    override val priority: Int = -1

    override fun canHandle(url: String): Boolean {
        return true // Handles everything as fallback
    }

    override fun generateAffiliateLink(url: String, subId: String?): String? {
        // Just return original
        return url
    }
}

/**
 * Example of a specific provider.
 * To be implemented properly later.
 */
class MockAffiliateProvider : AffiliateProvider {
    override val providerName: String = "TestProvider"
    override val priority: Int = 100

    override fun canHandle(url: String): Boolean {
        return url.contains("example.com")
    }

    override fun generateAffiliateLink(url: String, subId: String?): String? {
        val apiKey = com.offerlens.BuildConfig.AFFILIATE_AMAZON_TAG
        return "$url?ref=$apiKey&subId=$subId"
    }
}
