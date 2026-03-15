package com.offerlens.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.AuthRepository
import com.offerlens.data.User
import com.offerlens.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun completeOnboarding(
        selectedBanks: List<String>, 
        selectedPaymentTypes: List<String>, 
        onComplete: () -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        Timber.d("OnboardingViewModel", "completeOnboarding called with banks: $selectedBanks, types: $selectedPaymentTypes")
        viewModelScope.launch {
            try {
                // Sign in anonymously
                Timber.d("OnboardingViewModel", "Attempting anonymous sign in...")
                val userResult = authRepository.signInAnonymously()
                if (userResult.isSuccess) {
                    val firebaseUser = userResult.getOrThrow()
                    Timber.d("OnboardingViewModel", "Sign in successful, user ID: ${firebaseUser.uid}")
                    val user = User(
                        id = firebaseUser.uid,
                        preferredBanks = selectedBanks,
                        preferredPaymentTypes = selectedPaymentTypes,
                        email = firebaseUser.email ?: ""
                    )
                    Timber.d("OnboardingViewModel", "Saving user preferences...")
                    userRepository.saveUserPreferences(firebaseUser.uid, user)
                    Timber.d("OnboardingViewModel", "User preferences saved, calling onComplete")
                    onComplete()
                } else {
                    val error = userResult.exceptionOrNull() as? Exception ?: Exception("Unknown error")
                    Timber.e("OnboardingViewModel", "Sign in failed: ${error.message}", error)
                    onError(error)
                }
            } catch (e: Exception) {
                Timber.e("OnboardingViewModel", "Error in completeOnboarding: ${e.message}", e)
                onError(e)
            }
        }
    }
}
