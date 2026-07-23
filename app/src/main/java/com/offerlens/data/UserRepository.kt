package com.offerlens.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserPreferences(userId: String, user: User) {
        firestore.collection("users").document(userId)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Permanently deletes the user's Firestore document (preferences, premium flag).
     * Used by the in-app "Delete My Data" flow to satisfy DPDP Act erasure requests.
     */
    suspend fun deleteUserData(userId: String) {
        firestore.collection("users").document(userId).delete().await()
    }
}
