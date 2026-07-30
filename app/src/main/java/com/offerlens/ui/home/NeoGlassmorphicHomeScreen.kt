package com.offerlens.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import com.offerlens.ui.theme.Diamond
import com.offerlens.ui.theme.Wallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.offerlens.data.Offer
import com.offerlens.ui.theme.*
import com.offerlens.ui.components.NeonOfferCard
import com.offerlens.ui.components.GlassmorphicSearchBar
import com.offerlens.ui.components.NeonCategoryPill
import com.offerlens.ui.components.EmptyStateMessage
import com.offerlens.R
import androidx.compose.ui.res.stringResource
import java.util.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeoGlassmorphicHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    adManager: com.offerlens.data.AdManager? = null, // Injected via composition or manual passed
    onOfferClick: (String) -> Unit,
    onViewAllOffers: () -> Unit,
    onCalculatorClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onDataDeleted: () -> Unit = {}
) {
    val offers by viewModel.offers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState(initial = false)
    val isSmartWalletEnabled by viewModel.smartWalletEnabled.collectAsState()
    val userId = viewModel.userId
    val userName = viewModel.userName
    val userPhotoUrl = viewModel.userPhotoUrl
    var showUserDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeletingData by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val categories = remember { 
        listOf("All", "Dining", "Travel", "Shopping", "Entertainment", "Groceries", "Bill Pay & Recharges", "Wallet/UPI Offers") 
    }
    
    // Pager state for horizontal swiping
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val categoryListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Sync Pager <-> Category Selection
    // When Pager Changes -> Update ViewModel Category
    LaunchedEffect(pagerState.currentPage) {
        val category = categories[pagerState.currentPage]
        viewModel.updateCategory(category)
        
        // Scroll the category list to keep the selected item visible/centered
        categoryListState.animateScrollToItem(
            index = pagerState.currentPage,
            scrollOffset = -100 
        )
    }
    
    // When Search/Category changes in VM -> Update Pager (if needed, though mostly 1-way sync is enough for UI control)
    // Actually, simply relying on manual clicks to sync is safer to avoid loops.

    // Back Handler Logic
    BackHandler(enabled = searchQuery.isNotEmpty() || pagerState.currentPage != 0) {
        if (searchQuery.isNotEmpty()) {
            viewModel.updateSearchQuery("")
            focusManager.clearFocus()
        } else {
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(100f, 100f)
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed Top Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Five 40dp buttons plus 8dp gaps need ~232dp, which leaves too
                    // little for the title on a 360dp screen - the icons crowded it and
                    // the row overflowed. The title now yields space instead of the row
                    // overflowing, and the buttons sit tighter.
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        // User Profile / ID Button
                        IconButton(
                            onClick = { showUserDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            if (userPhotoUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(userPhotoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = stringResource(R.string.cd_user_profile),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.cd_user_profile),
                                    tint = RoyalGreen,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onCalculatorClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Savings Calculator",
                                tint = RoyalOrange
                            )
                        }

                        IconButton(
                            onClick = onViewAllOffers,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = stringResource(R.string.cd_premium),
                                tint = RoyalGold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshOffers() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.cd_refresh),
                                tint = RoyalGreen
                            )
                        }

                        IconButton(
                            onClick = onAboutClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About & Legal",
                                tint = RoyalGreen
                            )
                        }
                    }
                }
                
                // Search Bar
                // Search Bar
                GlassmorphicSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onClear = { 
                        viewModel.updateSearchQuery("") 
                        focusManager.clearFocus()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Smart Wallet Toggle (Premium Only)
                if (isPremium) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wallet, 
                                contentDescription = null, 
                                tint = if (isSmartWalletEnabled) NeonCyan else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.smart_wallet_filter), 
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = isSmartWalletEnabled,
                            onCheckedChange = { viewModel.toggleSmartWallet() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category Pills with synced state
                LazyRow(
                    state = categoryListState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(categories) { index, category ->
                        NeonCategoryPill(
                            text = category,
                            isSelected = pagerState.currentPage == index,
                            onClick = { 
                                scope.launch { 
                                    pagerState.animateScrollToPage(index) 
                                }
                            }
                        )
                    }
                }
            }
            
            // Swipeable Content Area
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                key = { index -> categories[index] } // Stable key for Pager
            ) { pageIndex ->
                // Division of labour: the ViewModel filters by search/premium/wallet (the
                // expensive, global concerns), and each pager page filters its own
                // category locally. The pager renders neighbouring pages, so per-page
                // category filtering is what keeps adjacent swipes showing the right
                // content - a single globally-category-filtered list would leave
                // neighbour pages empty or wrong mid-swipe.
                val currentCategory = categories[pageIndex]

                val categoryOffers = remember(offers, currentCategory) {
                    if (currentCategory == "All") {
                        offers
                    } else {
                        offers.filter { it.category.equals(currentCategory, ignoreCase = true) }
                    }
                }

                // Offers List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = categoryOffers,
                        key = { it.id } // STABLE KEY - Critical for performance
                    ) { offer ->
                        NeonOfferCard(
                            offer = offer,
                            onClick = { 
                                viewModel.logOfferClick(offer.id, offer.merchant)
                                // Show Ad before navigating (if not premium)
                                val activity = context as? android.app.Activity
                                if (activity != null) {
                                    viewModel.showInterstitial(activity)
                                }
                                onOfferClick(offer.id) 
                            }
                        )
                    }
                    
                    // Infinite Scroll Trigger
                    // Only for First Page ("All") and no search
                    if (categoryOffers.isNotEmpty() && pageIndex == 0 && searchQuery.isEmpty()) {
                        item(key = "loader") {
                             LaunchedEffect(Unit) {
                                viewModel.loadMoreOffers()
                            }
                        }
                    }
                    
                    if (categoryOffers.isEmpty()) {
                        item(key = "empty") {
                            EmptyStateMessage(
                                category = currentCategory,
                                isSearchActive = searchQuery.isNotBlank()
                            )
                        }
                    }
                }
            }
            
            com.offerlens.ui.components.AdBanner(
                isPremium = isPremium
            )
            
            if (showUserDialog && userId != null) {
                val context = LocalContext.current
                AlertDialog(
                    onDismissRequest = { showUserDialog = false },
                    title = { Text(text = stringResource(R.string.user_profile_title), color = MaterialTheme.colorScheme.onSurface) },
                    text = {
                        Column {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (userPhotoUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(userPhotoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .border(2.dp, NeonCyan, androidx.compose.foundation.shape.CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                if (!userName.isNullOrEmpty()) {
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(stringResource(R.string.user_id_label), color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("User ID", userId)
                                        clipboard.setPrimaryClip(clip)
                                    }
                            ) {
                            Text(
                                    text = userId,
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.tap_to_copy_hint), color = Color.DarkGray, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Delete My Data",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    showUserDialog = false
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("User ID", userId)
                            clipboard.setPrimaryClip(clip)
                            showUserDialog = false 
                        }) {
                            Text(stringResource(R.string.copy_id_button), color = NeonCyan)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUserDialog = false }) {
                            Text(stringResource(R.string.close_button), color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isDeletingData) showDeleteConfirmDialog = false },
                    title = { Text("Delete My Data?", color = MaterialTheme.colorScheme.error) },
                    text = {
                        Text(
                            "This permanently deletes your saved preferences and account from OfferLens. " +
                                "This cannot be undone. Any Play Store purchases are unaffected and can be " +
                                "restored later if you sign back in.",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !isDeletingData,
                            onClick = {
                                isDeletingData = true
                                viewModel.deleteMyData(
                                    onComplete = {
                                        isDeletingData = false
                                        showDeleteConfirmDialog = false
                                        Toast.makeText(context, "Your data has been deleted.", Toast.LENGTH_LONG).show()
                                        onDataDeleted()
                                    },
                                    onError = { error ->
                                        isDeletingData = false
                                        Toast.makeText(context, "Failed to delete data: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        ) {
                            Text(if (isDeletingData) "Deleting..." else "Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(enabled = !isDeletingData, onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Components moved to com.offerlens.ui.components package
// - NeoGlassmorphicHomeScreen now imports them
// - Hardcoded strings replaced with R.string.*

