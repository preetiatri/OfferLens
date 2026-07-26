package com.offerlens.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.android.billingclient.api.ProductDetails
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
    
    val productDetails: StateFlow<ProductDetails?> = premiumRepository.productDetails
    
    val price: kotlinx.coroutines.flow.Flow<String?> = productDetails.map { details ->
        details?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    fun launchPurchaseFlow(activity: Activity) {
        viewModelScope.launch {
            premiumRepository.launchPurchaseFlow(activity)
        }
    }
    
    private val _isRestoring = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring

    /** One-shot message for the UI to surface, cleared once shown. */
    private val _restoreMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val restoreMessage: StateFlow<String?> = _restoreMessage

    fun restorePurchases() {
        if (_isRestoring.value) return // ignore repeat taps while a query is in flight
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                _restoreMessage.value = when (val result = premiumRepository.restorePurchases()) {
                    is PremiumRepository.RestoreResult.Restored -> "Purchases restored 💎"
                    is PremiumRepository.RestoreResult.NothingToRestore ->
                        "No previous purchase found on this Google account"
                    is PremiumRepository.RestoreResult.Failed -> "Restore failed: ${result.message}"
                }
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun consumeRestoreMessage() {
        _restoreMessage.value = null
    }
}
