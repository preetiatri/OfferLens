package com.offerlens.data

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val preferredBanks: List<String> = emptyList(),
    val preferredPaymentTypes: List<String> = emptyList(),
    val isPremium: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentMethod(
    val bankName: String = "",
    val type: String = "", // Credit, Debit, etc.
    val network: String = "", // Visa, Mastercard
    val addedAt: Long = System.currentTimeMillis()
)
