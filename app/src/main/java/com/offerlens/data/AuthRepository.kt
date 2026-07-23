package com.offerlens.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: com.google.firebase.auth.AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    /**
     * Permanently deletes the current Firebase Auth account (used by the in-app
     * "Delete My Data" flow to satisfy DPDP Act erasure requests).
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No signed-in user"))
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
