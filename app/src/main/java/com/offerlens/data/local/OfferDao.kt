package com.offerlens.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Offer entities
 * Provides methods for offline data access
 */
@Dao
interface OfferDao {
    
    /**
     * Get all active offers from local cache
     */
    @Query("SELECT * FROM offers WHERE isActive = 1 ORDER BY createdAtSeconds DESC")
    fun getAllActiveOffers(): Flow<List<OfferEntity>>
    
    /**
     * Get all offers (including inactive) from local cache
     */
    @Query("SELECT * FROM offers ORDER BY createdAtSeconds DESC")
    fun getAllOffers(): Flow<List<OfferEntity>>
    
    /**
     * Get a specific offer by ID
     */
    @Query("SELECT * FROM offers WHERE id = :offerId LIMIT 1")
    suspend fun getOfferById(offerId: String): OfferEntity?
    
    /**
     * Get offers by category
     */
    @Query("SELECT * FROM offers WHERE category = :category AND isActive = 1 ORDER BY createdAtSeconds DESC")
    fun getOffersByCategory(category: String): Flow<List<OfferEntity>>
    
    /**
     * Get offers by bank name
     */
    @Query("SELECT * FROM offers WHERE bankName = :bankName AND isActive = 1 ORDER BY createdAtSeconds DESC")
    fun getOffersByBank(bankName: String): Flow<List<OfferEntity>>
    
    /**
     * Search offers by merchant name or description
     */
    @Query("SELECT * FROM offers WHERE (merchant LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isActive = 1 ORDER BY createdAtSeconds DESC")
    fun searchOffers(query: String): Flow<List<OfferEntity>>
    
    /**
     * Insert or update offers (replaces on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)
    
    /**
     * Insert or update a single offer
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity)
    
    /**
     * Delete all offers (for cache refresh)
     */
    @Query("DELETE FROM offers")
    suspend fun deleteAllOffers()
    
    /**
     * Delete offers older than specified timestamp
     */
    @Query("DELETE FROM offers WHERE cachedAt < :timestamp")
    suspend fun deleteOldOffers(timestamp: Long)

    /**
     * Delete offers that are NOT in the provided list of IDs (for syncing)
     * Keeps offers that are still valid/returned by API
     */
    @Query("DELETE FROM offers WHERE id NOT IN (:ids)")
    suspend fun deleteOffersNotIn(ids: List<String>)

    /**
     * Delete offers by category (useful for partial refresh)
     */
    @Query("DELETE FROM offers WHERE category = :category")
    suspend fun deleteOffersByCategory(category: String)
    
    /**
     * Get count of cached offers
     */
    @Query("SELECT COUNT(*) FROM offers")
    suspend fun getOfferCount(): Int
    
    /**
     * Get count of active offers
     */
    @Query("SELECT COUNT(*) FROM offers WHERE isActive = 1")
    suspend fun getActiveOfferCount(): Int
}
