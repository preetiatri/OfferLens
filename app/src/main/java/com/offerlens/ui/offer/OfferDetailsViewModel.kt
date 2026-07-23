package com.offerlens.ui.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.Offer
import com.offerlens.data.OfferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    private val offerRepository: OfferRepository
) : ViewModel() {

    private val _offer = MutableStateFlow<Offer?>(null)
    val offer: StateFlow<Offer?> = _offer.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadOffer(offerId: String) {
        Timber.d("Loading offer with ID: $offerId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val loadedOffer = offerRepository.getOfferById(offerId)
                if (loadedOffer != null) {
                    Timber.d("Offer loaded successfully: ${loadedOffer.merchant}")
                    _offer.value = loadedOffer
                } else {
                    Timber.e("Failed to load offer with ID: $offerId")
                    _offer.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
