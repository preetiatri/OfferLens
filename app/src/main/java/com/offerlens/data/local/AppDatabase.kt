package com.offerlens.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for OfferLens
 * Provides offline caching and faster data access
 */
// v4 added OfferEntity.tiersJson. There is no migration because this database is a
// disposable cache - fallbackToDestructiveMigration wipes it and the next sync refetches
// from Firestore. No user data lives here.
@Database(
    entities = [OfferEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun offerDao(): OfferDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = getSecurePassphrase(context)
                val factory = net.sqlcipher.database.SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offerlens_database"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getSecurePassphrase(context: Context, retryCount: Int = 0): ByteArray {
            val prefs = context.getSharedPreferences("app_secure_prefs", Context.MODE_PRIVATE)
            val alias = "OfferLensDBKey"

            try {
                val encryptedPassphrase = prefs.getString("db_passphrase", null)
                val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                
                if (!keyStore.containsAlias(alias)) {
                    val keyGenerator = android.security.keystore.KeyGenParameterSpec.Builder(
                        alias,
                        android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                    
                    val kg = javax.crypto.KeyGenerator.getInstance(
                        android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
                        "AndroidKeyStore"
                    )
                    kg.init(keyGenerator)
                    kg.generateKey()
                }
                
                val secretKey = keyStore.getKey(alias, null) as javax.crypto.SecretKey
                val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
                
                if (encryptedPassphrase == null) {
                    val rawPassphrase = ByteArray(32)
                    java.security.SecureRandom().nextBytes(rawPassphrase)
                    
                    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
                    val iv = cipher.iv
                    val encrypted = cipher.doFinal(rawPassphrase)
                    
                    val combined = iv + encrypted
                    prefs.edit().putString("db_passphrase", android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)).apply()
                    
                    val base64Passphrase = android.util.Base64.encodeToString(rawPassphrase, android.util.Base64.NO_WRAP)
                    return net.sqlcipher.database.SQLiteDatabase.getBytes(base64Passphrase.toCharArray())
                } else {
                    val combined = android.util.Base64.decode(encryptedPassphrase, android.util.Base64.NO_WRAP)
                    val iv = combined.copyOfRange(0, 12)
                    val encrypted = combined.copyOfRange(12, combined.size)
                    
                    val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, spec)
                    val rawPassphrase = cipher.doFinal(encrypted)
                    
                    val base64Passphrase = android.util.Base64.encodeToString(rawPassphrase, android.util.Base64.NO_WRAP)
                    return net.sqlcipher.database.SQLiteDatabase.getBytes(base64Passphrase.toCharArray())
                }
            } catch (e: Exception) {
                // If anything goes wrong with Keystore or decryption, we MUST reset to prevent crashes
                android.util.Log.e("AppDatabase", "Keystore recovery triggered (attempt $retryCount): ${e.message}")
                prefs.edit().remove("db_passphrase").apply()
                // Also attempt to delete the database file to ensure fresh start
                context.deleteDatabase("offerlens_database")

                if (retryCount >= 1) {
                    // Keystore is persistently broken (e.g. known OEM bugs). Bail out of the
                    // retry loop and hand back an ephemeral, non-persisted passphrase so the
                    // app can still open a (session-only) encrypted DB instead of crash-looping.
                    android.util.Log.e("AppDatabase", "Keystore persistently unavailable, using ephemeral passphrase")
                    val rawPassphrase = ByteArray(32)
                    java.security.SecureRandom().nextBytes(rawPassphrase)
                    val base64Passphrase = android.util.Base64.encodeToString(rawPassphrase, android.util.Base64.NO_WRAP)
                    return net.sqlcipher.database.SQLiteDatabase.getBytes(base64Passphrase.toCharArray())
                }

                // One bounded retry: the wipe above may be enough to recover a fresh key.
                return getSecurePassphrase(context, retryCount + 1)
            }
        }
    }
}
