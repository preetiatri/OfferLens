package com.offerlens.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

private val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore(name = "wallet_prefs")

@Singleton
class SmartWalletRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val MY_CARDS_KEY = stringSetPreferencesKey("my_cards")
    private val FILTER_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("smart_filter_enabled")

    private val _myCards = MutableStateFlow<Set<String>>(emptySet())
    val myCards: StateFlow<Set<String>> = _myCards.asStateFlow()

    /**
     * Whether the Smart Wallet filter is switched on. Persisted alongside the card
     * selection - previously this lived only in the ViewModel, so the selected cards
     * survived a restart but the filter silently switched itself off every launch.
     */
    private val _isFilterEnabled = MutableStateFlow(false)
    val isFilterEnabled: StateFlow<Boolean> = _isFilterEnabled.asStateFlow()

    companion object {
        /**
         * Words that appear across many issuer names and therefore say nothing about
         * which issuer this is. Matching on them made every bank match every other:
         * "HDFC Bank" and "Axis Bank" share "bank", so selecting one card surfaced
         * every bank's offers and the Smart Wallet filter appeared to do nothing.
         */
        private val GENERIC_ISSUER_WORDS = setOf(
            "bank", "banks", "card", "cards", "credit", "debit", "pay",
            "ltd", "limited", "india", "the", "and", "of", "upi", "wallet",
            // "Small Finance Bank" is a licence category shared by several issuers
            // (AU, Equitas, Ujjivan...) - matching on it would cross-match them.
            "small", "finance"
        )

        private fun distinctiveTokens(name: String): List<String> =
            name.lowercase().trim()
                .split(' ', '-', '/', '.', ',')
                .map { it.trim() }
                // >= 2 rather than > 2 so genuine short issuer tokens like "AU"
                // survive; two-letter filler ("of") is caught by the generic list.
                .filter { it.length >= 2 && it !in GENERIC_ISSUER_WORDS }

        /**
         * Whether an offer's bank name refers to the same issuer as a card the user holds.
         *
         * Exact match first, then a comparison of distinctive tokens only, so
         * "HDFC Bank" still matches an offer tagged "HDFC Credit Card" without also
         * matching "Axis Bank".
         */
        fun issuerMatches(offerBankName: String, userCard: String): Boolean {
            val offer = offerBankName.lowercase().trim()
            val user = userCard.lowercase().trim()
            if (offer.isBlank() || user.isBlank()) return false
            if (offer == user) return true

            val offerTokens = distinctiveTokens(offer)
            val userTokens = distinctiveTokens(user)
            if (offerTokens.isEmpty() || userTokens.isEmpty()) return false

            return offerTokens.any { o ->
                userTokens.any { u -> o == u || o.contains(u) || u.contains(o) }
            }
        }

        // Supported issuers. MUST stay in sync with SUPPORTED_BANKS in
        // public/admin/manage-offers.html - offers are tagged from that list, and
        // Smart Wallet matching depends on the names agreeing.
        val supportedBanks = listOf(
            // Banks & card issuers
            "HDFC Bank",
            "SBI Card",
            "ICICI Bank",
            "Axis Bank",
            "Kotak Mahindra",
            "AU Bank",
            "IDFC First Bank",
            "Yes Bank",
            "IndusInd Bank",
            "RBL Bank",
            "Federal Bank",
            "Bank of Baroda",
            "Punjab National Bank",
            "Canara Bank",
            "Union Bank of India",
            "IDBI Bank",
            // Citibank is deliberately absent: Citi sold its India consumer business to
            // Axis Bank and the card migration completed in July 2024, so there is no
            // Citibank-issued Indian card left for an offer to apply to.
            "HSBC",
            "Standard Chartered",
            "American Express",
            "OneCard",
            "Slice",
            // UPI apps & wallets
            "Paytm",
            "PhonePe",
            "Google Pay",
            "Amazon Pay",
            "CRED",
            "Mobikwik",
            "Freecharge",
            "BHIM UPI"
        )
    }

    init {
        scope.launch {
            val prefs = context.walletDataStore.data.first()
            val stored = prefs[MY_CARDS_KEY] ?: emptySet()
            // Drop any saved issuer that has since left the supported list. Such a name no
            // longer appears in My Cards, so the user cannot deselect it - and if it were
            // their only saved card, Smart Wallet would filter every offer away with no way
            // back short of "Clear all". Citibank is the first of these; slice and Freecharge
            // have both restructured, so it will not be the last.
            val pruned = stored.filterTo(mutableSetOf()) { it in supportedBanks }
            _myCards.value = pruned
            if (pruned.size != stored.size) {
                context.walletDataStore.edit { it[MY_CARDS_KEY] = pruned }
            }
            _isFilterEnabled.value = prefs[FILTER_ENABLED_KEY] ?: false
        }
    }

    fun setFilterEnabled(enabled: Boolean) {
        _isFilterEnabled.value = enabled
        scope.launch {
            context.walletDataStore.edit { prefs ->
                prefs[FILTER_ENABLED_KEY] = enabled
            }
        }
    }

    /**
     * Wipes the card selection and filter flag. Called from "Delete My Data" - without
     * this, the next account on the device inherited the previous user's card list,
     * despite the privacy policy promising preference removal.
     */
    suspend fun clearAll() {
        _myCards.value = emptySet()
        _isFilterEnabled.value = false
        context.walletDataStore.edit { it.clear() }
    }

    fun toggleCard(bankName: String) {
        val current = _myCards.value.toMutableSet()
        if (current.contains(bankName)) {
            current.remove(bankName)
        } else {
            current.add(bankName)
        }
        updateCards(current)
    }

    private fun updateCards(newSet: Set<String>) {
        _myCards.value = newSet
        scope.launch {
            context.walletDataStore.edit { prefs ->
                prefs[MY_CARDS_KEY] = newSet
            }
        }
    }
}
