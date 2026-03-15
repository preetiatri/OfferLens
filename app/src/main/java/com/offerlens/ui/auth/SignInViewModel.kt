package com.offerlens.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.offerlens.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Timber.d("Attempting sign in with email: $email")
                
                // Sign in with email and password
                val result = auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            Timber.d("Sign in successful for user: ${user.uid}")
                            onSuccess()
                        } else {
                            Timber.e("Sign in failed: User is null")
                            onError(Exception("Sign in failed: User is null"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Sign in failed")
                        onError(exception)
                    }
                
            } catch (e: Exception) {
                Timber.e(e, "Error in signInWithEmail")
                onError(e)
            }
        }
    }
}
