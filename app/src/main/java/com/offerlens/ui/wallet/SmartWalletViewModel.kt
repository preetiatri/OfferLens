package com.offerlens.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.SmartWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartWalletViewModel @Inject constructor(
    private val repository: SmartWalletRepository
) : ViewModel() {

    val myCards: StateFlow<Set<String>> = repository.myCards
    val supportedBanks: List<String> = SmartWalletRepository.supportedBanks

    fun toggleCard(bankName: String) {
        viewModelScope.launch {
            repository.toggleCard(bankName)
        }
    }
}
