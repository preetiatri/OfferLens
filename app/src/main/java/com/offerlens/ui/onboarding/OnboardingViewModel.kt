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
        Timber.d("completeOnboarding called with banks: $selectedBanks, types: $selectedPaymentTypes")
        viewModelScope.launch {
            try {
                // Sign in anonymously
                Timber.d("Attempting anonymous sign in...")
                val userResult = authRepository.signInAnonymously()
                if (userResult.isSuccess) {
                    val firebaseUser = userResult.getOrThrow()
                    Timber.d("Sign in successful, user ID: ${firebaseUser.uid}")
                    val user = User(
                        id = firebaseUser.uid,
                        preferredBanks = selectedBanks,
                        preferredPaymentTypes = selectedPaymentTypes,
                        email = firebaseUser.email ?: ""
                    )
                    Timber.d("Saving user preferences...")
                    try {
                        userRepository.saveUserPreferences(firebaseUser.uid, user)
                        Timber.d("User preferences saved, calling onComplete")
                        onComplete()
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to save user preferences: ${e.message}")
                        onError(e)
                    }
                } else {
                    val error = userResult.exceptionOrNull() as? Exception ?: Exception("Unknown error")
                    Timber.e(error, "Sign in failed: ${error.message}")
                    onError(error)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in completeOnboarding: ${e.message}")
                onError(e)
            }
        }
    }
}
