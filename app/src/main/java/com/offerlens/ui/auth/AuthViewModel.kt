package com.offerlens.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.AuthRepository
import com.offerlens.data.User
import com.offerlens.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun signInAnonymously(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull() as? Exception ?: Exception("Unknown error"))
            }
        }
    }

    fun currentUser() = authRepository.currentUser
}
