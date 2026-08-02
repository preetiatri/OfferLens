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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    // Per-chunk content hashes from the last successful catalogue sync, joined with ",".
    // Comparing against the server's hashes lets a sync skip chunks that haven't changed,
    // so a typical day's sync is one tiny meta read and zero chunk downloads.
    private val KEY_CHUNK_HASHES = "catalogue_chunk_hashes"
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
                clearOldCache()
                Timber.d("Background sync completed")
            } catch (e: Exception) {
                Timber.e(e, "Background sync failed")
            }
        }
    }
    // Pagination state - guarded by paginationMutex since HomeViewModel and OfferListViewModel
    // can both call getOffers()/loadMoreOffers() concurrently on this shared singleton.
    private val paginationMutex = Mutex()
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
            Timber.d("========== FETCHING OFFERS (LoadMore: $isLastPageReached) ==========")
            
            // If just loading from cache (initial load)
            if (!forceRefresh && !isLoadMore) {
                val cached = getCachedOffers()
                if (cached.isNotEmpty()) {
                    Timber.d("Returning ${cached.size} cached offers")
                    
                    // Check Time-To-Live (TTL) Cache to prevent unnecessary Firebase reads
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
                    val now = System.currentTimeMillis()
                    
                    if (now - lastFetch < CACHE_TTL_MS) {
                        Timber.d("Cache is fresh (TTL active). Skipping Firebase fetch.")
                        return cached
                    }
                }
            }

            // Sync with Firebase. The check-then-fetch-then-advance-cursor sequence must be
            // atomic since this repository is a singleton shared by multiple ViewModels
            // (e.g. Home + OfferList) that can call getOffers()/loadMoreOffers() concurrently.
            val firebaseOffers = paginationMutex.withLock {
                if (isLoadMore) {
                    if (isLastPageReached) {
                        Timber.d("End of list reached, no more offers to fetch")
                        return getCachedOffers()
                    }
                    fetchFromFirebase(isLoadMore = true)
                } else {
                    // Fresh load. Prefer the published catalogue (catalogue/meta + chunk
                    // docs): one manifest read answers "did anything change?", and only
                    // changed chunks are downloaded. Per-document reads of the offers
                    // collection cost catalogue-size reads per user per sync, which blows
                    // through the Firestore free tier at under a hundred daily users.
                    lastVisibleDocument = null
                    isLastPageReached = true
                    if (syncViaCatalogue()) {
                        emptyList() // cache already reconciled and updated
                    } else {
                        // Catalogue not published yet - legacy full-collection fetch. The
                        // response is the whole active set, so it is safe to treat as
                        // authoritative and delete whatever is missing from it.
                        val all = fetchAllActiveFromFirebase()
                        reconcileCache(all)
                        all
                    }
                }
            }

            if (firebaseOffers.isNotEmpty()) {
                Timber.d("Caching ${firebaseOffers.size} offers")
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
            Timber.e(e, "Error fetching offers")
            getCachedOffers()
        }
    }
    
    /**
     * Syncs from the published catalogue written by the admin panel:
     *
     *   catalogue/meta     - { chunkCount, chunkHashes: [..], ids: [..], version }
     *   catalogue/chunk_N  - { offers: [plain maps], hash }
     *
     * Cost model, which is the whole point: the meta read is 1 document regardless of
     * catalogue size, meta.ids lets deletions reconcile without downloading anything,
     * and a chunk is only fetched when its content hash differs from the one stored at
     * the last sync. A day with no admin edits costs each user 1 read per sync instead
     * of catalogue-size reads.
     *
     * Returns false when the catalogue has never been published (meta missing), in which
     * case the caller falls back to reading the offers collection directly.
     */
    private suspend fun syncViaCatalogue(): Boolean {
        val meta = firestore.collection("catalogue").document("meta").get().await()
        if (!meta.exists()) return false

        val ids = (meta.get("ids") as? List<*>)?.filterIsInstance<String>() ?: return false
        val serverHashes = (meta.get("chunkHashes") as? List<*>)?.map { it.toString() } ?: emptyList()

        // Deletions first, straight from the manifest - a deactivated offer disappears
        // from meta.ids even when no chunk needs downloading.
        val idSet = ids.toSet()
        val staleIds = offerDao.getAllCachedIds().filterNot { it in idSet }
        if (staleIds.isNotEmpty()) {
            staleIds.chunked(500).forEach { offerDao.deleteOffersByIds(it) }
            Timber.d("Catalogue sync removed ${staleIds.size} offer(s) no longer published")
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedHashes = prefs.getString(KEY_CHUNK_HASHES, "")!!
            .split(",").filter { it.isNotEmpty() }

        var fetched = 0
        serverHashes.forEachIndexed { i, hash ->
            if (storedHashes.getOrNull(i) != hash) {
                val chunk = firestore.collection("catalogue").document("chunk_$i").get().await()
                val offers = (chunk.get("offers") as? List<*>)
                    ?.filterIsInstance<Map<String, Any?>>()
                    ?.mapNotNull { mapToOffer(it) }
                    ?: emptyList()
                if (offers.isNotEmpty()) {
                    offerDao.insertOffers(offers.map { it.toEntity() })
                    fetched += offers.size
                }
            }
        }
        // Persisted only after every changed chunk landed, so a failed sync retries the
        // same chunks next time instead of recording progress it didn't make. (An
        // exception above propagates to getOffers' catch, which serves cache.)
        prefs.edit().putString(KEY_CHUNK_HASHES, serverHashes.joinToString(",")).apply()
        Timber.d("Catalogue sync done (${serverHashes.size} chunk(s), $fetched offer(s) refreshed)")
        return true
    }

    private fun msToTimestamp(v: Any?): com.google.firebase.Timestamp? {
        val ms = (v as? Number)?.toLong() ?: return null
        return com.google.firebase.Timestamp(ms / 1000, ((ms % 1000) * 1_000_000).toInt())
    }

    /**
     * Rebuilds an Offer from the plain map stored in a catalogue chunk. Timestamps travel
     * as epoch milliseconds (Firestore Timestamp objects don't survive inside arrays the
     * admin panel serializes with JSON.stringify for hashing).
     */
    private fun mapToOffer(m: Map<String, Any?>): Offer? {
        val id = m["id"] as? String ?: return null
        return try {
            Offer(
                id = id,
                bankName = m["bankName"] as? String ?: "",
                paymentType = m["paymentType"] as? String ?: "",
                merchant = m["merchant"] as? String ?: "",
                discountType = m["discountType"] as? String ?: "",
                discountValue = (m["discountValue"] as? Number)?.toDouble() ?: 0.0,
                maxDiscountAmount = (m["maxDiscountAmount"] as? Number)?.toDouble(),
                minOrderValue = (m["minOrderValue"] as? Number)?.toDouble(),
                tiers = (m["tiers"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()?.map { t ->
                    OfferTier(
                        label = t["label"] as? String ?: "",
                        discountValue = (t["discountValue"] as? Number)?.toDouble() ?: 0.0,
                        maxDiscountAmount = (t["maxDiscountAmount"] as? Number)?.toDouble(),
                        minOrderValue = (t["minOrderValue"] as? Number)?.toDouble(),
                        note = t["note"] as? String ?: ""
                    )
                } ?: emptyList(),
                startDate = msToTimestamp(m["startDateMs"]),
                endDate = msToTimestamp(m["endDateMs"]),
                isActive = true, // only active offers are published
                description = m["description"] as? String ?: "",
                merchantUrl = m["merchantUrl"] as? String ?: "",
                offerSourceUrl = m["offerSourceUrl"] as? String ?: "",
                category = m["category"] as? String ?: "",
                couponCode = m["couponCode"] as? String ?: "",
                couponRevealedOnSite = m["couponRevealedOnSite"] as? Boolean ?: false,
                termsAndConditions = m["termsAndConditions"] as? String ?: "",
                createdAt = msToTimestamp(m["createdAtMs"]),
                updatedAt = msToTimestamp(m["updatedAtMs"])
            )
        } catch (e: Exception) {
            Timber.e("Error parsing catalogue offer $id: ${e.message}")
            null
        }
    }

    /**
     * Fetches every active offer in one query, for use as the authoritative set on a
     * fresh sync. The catalogue is in the low hundreds, so this is a single small read
     * rather than something that needs paging.
     */
    private suspend fun fetchAllActiveFromFirebase(): List<Offer> {
        val snapshot = firestore.collection("offers")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        Timber.d("Full sync fetched ${snapshot.documents.size} active offers")

        return snapshot.documents.mapNotNull { document ->
            try {
                document.toObject(Offer::class.java)?.copy(id = document.id)
            } catch (e: Exception) {
                Timber.e("Error parsing offer ${document.id}: ${e.message}")
                null
            }
        }
    }

    /**
     * Removes cached offers the server no longer returns - the ones deactivated or deleted
     * in the admin panel. Without this they linger in Room with their stale isActive=1 and
     * keep showing in the app, since an insert-only sync never deletes anything.
     *
     * Note this runs even when the server returns nothing: an emptied catalogue must empty
     * the cache too. A failed query throws rather than returning empty, so this cannot be
     * triggered by a network error.
     */
    private suspend fun reconcileCache(serverOffers: List<Offer>) {
        try {
            val serverIds = serverOffers.map { it.id }.toSet()
            val staleIds = offerDao.getAllCachedIds().filterNot { it in serverIds }
            if (staleIds.isEmpty()) return

            // Chunked to stay under SQLite's 999 bound-variable limit.
            staleIds.chunked(500).forEach { offerDao.deleteOffersByIds(it) }
            Timber.d("Reconcile removed ${staleIds.size} offer(s) no longer on the server")
        } catch (e: Exception) {
            Timber.e(e, "Cache reconcile failed")
        }
    }

    /**
     * Fetch offers from Firebase Firestore with Pagination
     */
    private suspend fun fetchFromFirebase(isLoadMore: Boolean): List<Offer> {
        Timber.d("Querying Firestore (Page Size: $PAGE_SIZE)...")
        
        var query = firestore.collection("offers")
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING) // Ordered by date
            .limit(PAGE_SIZE)

        if (isLoadMore && lastVisibleDocument != null) {
            query = query.startAfter(lastVisibleDocument!!)
            Timber.d("Fetching NEXT page...")
        } else if (!isLoadMore) {
            Timber.d("Fetching FIRST page (resetting cursor)...")
            lastVisibleDocument = null
            isLastPageReached = false
        }

        val snapshot = query.get().await()
        
        if (!snapshot.isEmpty) {
            lastVisibleDocument = snapshot.documents[snapshot.size() - 1]
            if (snapshot.size() < PAGE_SIZE) {
                isLastPageReached = true
                Timber.d("Reached end of offers.")
            }
        } else {
            isLastPageReached = true
        }
        
        Timber.d("Fetched: ${snapshot.documents.size} docs")
        
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
     * Drops offers whose end date has passed.
     *
     * Deliberately client-side and applied on every read: an offer expiring tonight must
     * disappear tomorrow morning without waiting for an admin to deactivate it, and
     * showing a dead offer is the fastest way to lose a user's trust in a deals app.
     *
     * A null endDate means "no published expiry" (common for ongoing bank tie-ups) and is
     * always kept - never confuse "unknown" with "expired".
     *
     * The cutoff is the START of today, so an offer valid "until 30 Sep" is still shown
     * throughout 30 Sep rather than vanishing at midnight when its timestamp is reached.
     */
    private fun List<Offer>.excludeExpired(): List<Offer> {
        val startOfToday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return filter { offer ->
            val end = offer.endDate?.toDate()?.time
            end == null || end >= startOfToday
        }
    }

    /**
     * Get cached offers from Room database
     */
    private suspend fun getCachedOffers(): List<Offer> {
        return try {
            // Use first() to get the current snapshot of data without waiting for updates
            // This prevents the infinite suspension caused by collect()
            val entities = offerDao.getAllActiveOffers().first()
            if (entities.isNotEmpty()) {
                Timber.d("Found ${entities.size} offers in cache")
            }
            val all = entities.map { it.toOffer() }
            val live = all.excludeExpired()
            if (live.size < all.size) {
                Timber.d("Hid ${all.size - live.size} expired offer(s)")
            }
            live
        } catch (e: Exception) {
            Timber.e(e, "Error fetching cached offers")
            emptyList()
        }
    }
    
    /**
     * Get offers as Flow for reactive updates
     */
    fun getOffersFlow(): Flow<List<Offer>> {
        return offerDao.getAllActiveOffers().map { entities ->
            entities.map { it.toOffer() }.excludeExpired()
        }
    }
    
    suspend fun getOfferById(offerId: String): Offer? {
        // Cache first: the catalogue sync keeps every active offer in Room, so the detail
        // screen almost always opens something we already have. Reading Firestore first
        // cost one billed read per detail view - at a few thousand daily users that alone
        // was tens of thousands of reads a day for data already sitting on the device.
        val cached = offerDao.getOfferById(offerId)
        if (cached != null) return cached.toOffer()

        // Cache miss (deep link to an offer not yet synced, or cleared storage) - fall
        // back to a direct read.
        return try {
            Timber.d("Cache miss for offer $offerId, fetching from Firestore")
            val snapshot = firestore.collection("offers")
                .document(offerId)
                .get()
                .await()
            val offer = snapshot.toObject(Offer::class.java)?.copy(id = snapshot.id)
            if (offer != null) offerDao.insertOffer(offer.toEntity())
            offer
        } catch (e: Exception) {
            Timber.e(e, "Error fetching offer by ID: $offerId")
            null
        }
    }

    
    /**
     * Clear old cached offers (older than 7 days)
     */
    suspend fun clearOldCache() {
        try {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            offerDao.deleteOldOffers(sevenDaysAgo)
            Timber.d("Cleared old cached offers")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing old cache")
        }
    }
}
