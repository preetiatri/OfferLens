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
        // Write an explicit field map rather than the User object. Serializing the data
        // class includes isPremium (Kotlin emits defaulted fields), and the Firestore
        // create rule rejects any client write whose keys include isPremium or admin -
        // so every NEW user's preference save was denied outright, and an update by a
        // user an admin had granted premium would have been denied too (writing
        // isPremium=false over true counts as changing a protected key).
        val data = mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "preferredBanks" to user.preferredBanks,
            "preferredPaymentTypes" to user.preferredPaymentTypes,
            "createdAt" to user.createdAt
        )
        firestore.collection("users").document(userId)
            .set(data, SetOptions.merge())
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
