package com.offerlens.ui.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offerlens.data.AuthRepository
import com.offerlens.data.Offer
import com.offerlens.data.OfferRepository
import com.offerlens.data.User
import com.offerlens.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

import com.offerlens.data.SampleDataSeeder

@OptIn(FlowPreview::class)
@HiltViewModel
class OfferListViewModel @Inject constructor(
    private val offerRepository: OfferRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sampleDataSeeder: SampleDataSeeder
) : ViewModel() {

    private val _allOffers = MutableStateFlow<List<Offer>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    private val _userPreferences = MutableStateFlow<User?>(null)
    val userPreferences: StateFlow<User?> = _userPreferences.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Debounced search query
    private val debouncedSearchQuery = _searchQuery
        .debounce(300) // 300ms debounce
        .distinctUntilChanged()

    // Filtered offers based on search and category ONLY (no user preference filtering)
    val offers: StateFlow<List<Offer>> = combine(
        _allOffers,
        debouncedSearchQuery,
        _selectedCategory
    ) { allOffers, query, category ->
        filterOffers(allOffers, query, category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadUserPreferences()
        loadOffers()
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser?.uid
                if (userId != null) {
                    val user = userRepository.getUser(userId)
                    _userPreferences.value = user
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user preferences")
            }
        }
    }

    private fun loadOffers() {
        viewModelScope.launch {
            try {
                _allOffers.value = offerRepository.getOffers()
                Timber.d("Loaded ${_allOffers.value.size} offers from Firestore")
            } catch (e: Exception) {
                Timber.e(e, "Error loading offers")
            }
        }
    }

    fun loadMoreOffers() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            try {
                _isLoadingMore.value = true
                _allOffers.value = offerRepository.getOffers(isLoadMore = true)
                Timber.d("Loaded more offers. Total: ${_allOffers.value.size}")
            } catch (e: Exception) {
                Timber.e(e, "Error loading more offers")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    private fun filterOffers(
        allOffers: List<Offer>,
        query: String,
        category: String?
    ): List<Offer> {
        var filtered = allOffers
        Timber.d("Filtering ${allOffers.size} offers. Query: '$query', Category: '$category'")

        // NO USER PREFERENCE FILTERING - Show all offers regardless of bank/payment type
        
        // Filter by category only
        if (category != null) {
            filtered = filtered.filter { offer ->
                offer.category.equals(category, ignoreCase = true)
            }
            Timber.d("After Category Filter: ${filtered.size} offers")
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val searchLower = query.lowercase()
            filtered = filtered.filter { offer ->
                // Search in bank name
                offer.bankName.lowercase().contains(searchLower) ||
                // Search in payment method
                offer.paymentType.lowercase().contains(searchLower) ||
                // Search in merchant name
                offer.merchant.lowercase().contains(searchLower) ||
                // Search in description
                offer.description.lowercase().contains(searchLower) ||
                // Search in category (tags)
                offer.category.lowercase().contains(searchLower) ||
                // Search in coupon code
                offer.couponCode.lowercase().contains(searchLower) ||
                // Search by validity (check if offer is active)
                (searchLower.contains("active") && offer.isActive) ||
                (searchLower.contains("expired") && !offer.isActive)
            }
            Timber.d("After Search Filter: ${filtered.size} offers")
        }

        Timber.d("Returning ${filtered.size} offers")
        return filtered
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun seedSampleData() {
        viewModelScope.launch {
            try {
                sampleDataSeeder.seedData()
                loadOffers() // Reload offers after seeding
            } catch (e: Exception) {
                Timber.e(e, "Error seeding data")
            }
        }
    }
}
