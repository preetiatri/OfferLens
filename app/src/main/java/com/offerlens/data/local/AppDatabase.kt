package com.offerlens.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for OfferLens
 * Provides offline caching and faster data access
 */
@Database(
    entities = [OfferEntity::class],
    version = 3,
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

        private fun getSecurePassphrase(context: Context): ByteArray {
            val prefs = context.getSharedPreferences("app_secure_prefs", Context.MODE_PRIVATE)
            val encryptedPassphrase = prefs.getString("db_passphrase", null)
            
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val alias = "OfferLensDBKey"
            
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
                prefs.edit().putString("db_passphrase", android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)).apply()
                
                return net.sqlcipher.database.SQLiteDatabase.getBytes(android.util.Base64.encodeToString(rawPassphrase, android.util.Base64.DEFAULT).toCharArray())
            } else {
                val combined = android.util.Base64.decode(encryptedPassphrase, android.util.Base64.DEFAULT)
                val iv = combined.copyOfRange(0, 12)
                val encrypted = combined.copyOfRange(12, combined.size)
                
                val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, spec)
                val rawPassphrase = cipher.doFinal(encrypted)
                return net.sqlcipher.database.SQLiteDatabase.getBytes(android.util.Base64.encodeToString(rawPassphrase, android.util.Base64.DEFAULT).toCharArray())
            }
        }
    }
}
