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
        // Predefined List of Banks/Cards supported
        val supportedBanks = listOf(
            "HDFC Bank",
            "SBI Card",
            "ICICI Bank",
            "Axis Bank",
            "Kotak Mahindra",
            "Citibank",
            "American Express",
            "Standard Chartered",
            "RBL Bank",
            "IndusInd Bank",
            "OneCard",
            "Amazon Pay"
        )
    }

    init {
        scope.launch {
            val prefs = context.walletDataStore.data.first()
            _myCards.value = prefs[MY_CARDS_KEY] ?: emptySet()
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
