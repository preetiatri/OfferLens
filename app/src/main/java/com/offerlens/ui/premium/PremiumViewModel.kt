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
    
    fun restorePurchases(activity: Activity) {
        viewModelScope.launch {
            premiumRepository.restorePurchases(activity)
        }
    }
}
