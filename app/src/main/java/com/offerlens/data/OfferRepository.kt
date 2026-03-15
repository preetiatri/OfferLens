package com.offerlens.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.offerlens.data.local.OfferDao
import com.offerlens.data.local.toEntity
import com.offerlens.data.local.toOffer
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.*
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfferRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val offerDao: OfferDao
) {
    private val PREFS_NAME = "offer_repo_prefs"
    private val KEY_LAST_FETCH = "last_fetch_timestamp"
    private val CACHE_TTL_MS = 12 * 60 * 60 * 1000L // 12 hours

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        syncOnStartup()
    }

    private fun syncOnStartup() {
        repositoryScope.launch {
            try {
                Timber.d("Starting background sync for offers...")
                // Initial fetch to populate/refresh cache
                getOffers(forceRefresh = true)
                Timber.d("Background sync completed")
            } catch (e: Exception) {
                Timber.e("Background sync failed: ${e.message}")
            }
        }
    }
    // Pagination state
    private var lastVisibleDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLastPageReached = false
    private val PAGE_SIZE = 20L

    /**
     * Get offers with offline-first approach
     * Returns cached data immediately, then syncs with Firebase in background
     */
    suspend fun getOffers(
        banks: List<String> = emptyList(),
        paymentTypes: List<String> = emptyList(),
        forceRefresh: Boolean = false,
        isLoadMore: Boolean = false
    ): List<Offer> {
        return try {
            Timber.d("OfferRepository", "========== FETCHING OFFERS (LoadMore: $isLastPageReached) ==========")
            
            // If just loading from cache (initial load)
            if (!forceRefresh && !isLoadMore) {
                val cached = getCachedOffers()
                if (cached.isNotEmpty()) {
                    Timber.d("OfferRepository", "Returning ${cached.size} cached offers")
                    
                    // Check Time-To-Live (TTL) Cache to prevent unnecessary Firebase reads
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
                    val now = System.currentTimeMillis()
                    
                    if (now - lastFetch < CACHE_TTL_MS) {
                        Timber.d("OfferRepository", "Cache is fresh (TTL active). Skipping Firebase fetch.")
                        return cached
                    }
                }
            }

            // Sync with Firebase
            if (isLoadMore && isLastPageReached) {
                Timber.d("OfferRepository", "End of list reached, no more offers to fetch")
                return getCachedOffers()
            }

            val firebaseOffers = fetchFromFirebase(isLoadMore)
            
            // Re-enabled: Clear cache on fresh load (Page 1) to ensure we remove Deactivated offers.
            // If we don't do this, offers deactivated in the Admin Panel will remain "Active" in the local cache.
            if (firebaseOffers.isNotEmpty()) {
                // Re-enabled: Clear cache on fresh load (Page 1) to ensure we remove Deactivated offers.
                // If we don't do this, offers deactivated in the Admin Panel will remain "Active" in the local cache.
                if (!isLoadMore) {
                    Timber.d("OfferRepository", "Fresh load - Updating cache safely")
                    // CRITICAL FIX: Do NOT delete all offers. 
                    // Instead, we should ideally only delete offers that are no longer valid.
                    // However, since we don't have a "sync tokens" mechanism yet, a full wipe was the naive approach.
                    // Improved approach: 
                    // 1. If we are fetching "All" (no specific query/category), we *could* treat the new list as the "Truth" for the top items.
                    // But we can't know if items deeper in the list are deleted.
                    
                    // Best compromise for now without full Sync Engine:
                    // Just Insert/Update. Old invalid offers might linger until `deleteOldOffers` cleans them up.
                    // This prevents wiping "Favorites" or "Other Categories" if we were fetching a specific category.
                    
                    // We will NOT call deleteAllOffers().
                    // We rely on `insertOffers` (REPLACE strategy) to update existing ones.
                    // We rely on `clearOldCache()` (called periodically) to remove stale ones.
                }

                Timber.d("OfferRepository", "Caching ${firebaseOffers.size} offers")
                offerDao.insertOffers(firebaseOffers.map { it.toEntity() })
            }
            
            // Update TTL timestamp on a fresh fetch attempt
            if (!isLoadMore) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putLong(KEY_LAST_FETCH, System.currentTimeMillis()).apply()
            }
            
            // Return all currently cached offers (which now includes the new ones)
            getCachedOffers()
        } catch (e: Exception) {
            Timber.e("OfferRepository", "Error: ${e.message}")
            getCachedOffers()
        }
    }
    
    /**
     * Fetch offers from Firebase Firestore with Pagination
     */
    private suspend fun fetchFromFirebase(isLoadMore: Boolean): List<Offer> {
        Timber.d("OfferRepository", "Querying Firestore (Page Size: $PAGE_SIZE)...")
        
        var query = firestore.collection("offers")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING) // Ordered by date
            .limit(PAGE_SIZE)

        if (isLoadMore && lastVisibleDocument != null) {
            query = query.startAfter(lastVisibleDocument!!)
            Timber.d("OfferRepository", "Fetching NEXT page...")
        } else if (!isLoadMore) {
            Timber.d("OfferRepository", "Fetching FIRST page (resetting cursor)...")
            lastVisibleDocument = null
            isLastPageReached = false
        }

        val snapshot = query.get().await()
        
        if (!snapshot.isEmpty) {
            lastVisibleDocument = snapshot.documents[snapshot.size() - 1]
            if (snapshot.size() < PAGE_SIZE) {
                isLastPageReached = true
                Timber.d("OfferRepository", "Reached end of offers.")
            }
        } else {
            isLastPageReached = true
        }
        
        Timber.d("OfferRepository", "Fetched: ${snapshot.documents.size} docs")
        
        return snapshot.documents.mapNotNull { document ->
            try {
                val offer = document.toObject(Offer::class.java)
                offer?.copy(id = document.id)
            } catch (e: Exception) {
                Timber.e("Error parsing offer ${document.id}: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Get cached offers from Room database
     */
    /**
     * Get cached offers from Room database
     */
    private suspend fun getCachedOffers(): List<Offer> {
        return try {
            // Use first() to get the current snapshot of data without waiting for updates
            // This prevents the infinite suspension caused by collect()
            val entities = offerDao.getAllActiveOffers().first()
            if (entities.isNotEmpty()) {
                Timber.d("OfferRepository", "Found ${entities.size} offers in cache")
            }
            entities.map { it.toOffer() }
        } catch (e: Exception) {
            Timber.e("OfferRepository", "Error fetching cached offers", e)
            emptyList()
        }
    }
    
    /**
     * Get offers as Flow for reactive updates
     */
    fun getOffersFlow(): Flow<List<Offer>> {
        return offerDao.getAllActiveOffers().map { entities ->
            entities.map { it.toOffer() }
        }
    }
    
    suspend fun getOfferById(offerId: String): Offer? {
        return try {
            Timber.d("OfferRepository", "Fetching offer with ID: $offerId")
            
            // Try Firebase first
            val snapshot = firestore.collection("offers")
                .document(offerId)
                .get()
                .await()
            
            Timber.d("OfferRepository", "Document exists: ${snapshot.exists()}")
            
            val offer = snapshot.toObject(Offer::class.java)?.copy(id = snapshot.id)
            if (offer != null) {
                Timber.d("OfferRepository", "Fetched offer from Firebase: ${offer.merchant} (ID: ${offer.id})")
                // Cache it
                offerDao.insertOffer(offer.toEntity())
            } else {
                Timber.w("OfferRepository", "Offer not found in Firebase, checking cache...")
                // Fallback to cache
                val cachedEntity = offerDao.getOfferById(offerId)
                return cachedEntity?.toOffer()
            }
            offer
        } catch (e: Exception) {
            Timber.e("OfferRepository", "Error fetching offer by ID: $offerId, checking cache", e)
            // Fallback to cache
            val cachedEntity = offerDao.getOfferById(offerId)
            cachedEntity?.toOffer()
        }
    }

    suspend fun addOffers(offers: List<Offer>) {
        try {
            // Add to Firebase
            val batch = firestore.batch()
            offers.forEach { offer ->
                val docRef = firestore.collection("offers").document()
                batch.set(docRef, offer)
            }
            batch.commit().await()
            Timber.d("OfferRepository", "Successfully added ${offers.size} offers to Firebase")
            
            // Also cache locally
            offerDao.insertOffers(offers.map { it.toEntity() })
            Timber.d("OfferRepository", "Successfully cached ${offers.size} offers locally")
        } catch (e: Exception) {
            Timber.e("OfferRepository", "Error adding offers", e)
            throw e
        }
    }
    
    /**
     * Clear old cached offers (older than 7 days)
     */
    suspend fun clearOldCache() {
        try {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            offerDao.deleteOldOffers(sevenDaysAgo)
            Timber.d("OfferRepository", "Cleared old cached offers")
        } catch (e: Exception) {
            Timber.e("OfferRepository", "Error clearing old cache", e)
        }
    }
}
