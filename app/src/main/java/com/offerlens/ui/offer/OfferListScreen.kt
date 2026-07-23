package com.offerlens.ui.offer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.offerlens.ui.theme.ContentCopy
import androidx.compose.material.icons.filled.Search
import com.offerlens.ui.theme.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber
import com.offerlens.data.Offer
import com.offerlens.ui.components.NeonOfferCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OfferListScreen(
    viewModel: OfferListViewModel = hiltViewModel(),
    onOfferClick: (String) -> Unit
) {
    val offers by viewModel.offers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    
    // Categories including "All" and "Bill Pay & Recharges"
    val categories = listOf(
        "All",
        "Dining", 
        "Travel", 
        "Shopping", 
        "Entertainment",
        "Groceries",
        "Bill Pay & Recharges",
        "Wallet/UPI Offers"
    )
    
    // Debug logging
    LaunchedEffect(Unit) {
        Timber.tag("OfferListScreen").d("Categories loaded: ${categories.joinToString()}")
        Timber.tag("OfferListScreen").d("Total categories: ${categories.size}")
    }
    
    // Pager state for horizontal swiping
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { categories.size }
    )
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Debug pager state
    LaunchedEffect(pagerState.currentPage) {
        Timber.tag("OfferListScreen").d("Current page: ${pagerState.currentPage}, Category: ${categories.getOrNull(pagerState.currentPage)}")
        // Scroll the tab row to keep the selected category visible
        listState.animateScrollToItem(pagerState.currentPage)
    }
    
    // Sync pager state with selected category
    LaunchedEffect(selectedCategory) {
        val index = if (selectedCategory == null) {
            0 // "All" category
        } else {
            categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
        }
        Timber.tag("OfferListScreen").d("Selected category changed to: $selectedCategory, index: $index")
        if (pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }
    
    // Update selected category when pager changes
    LaunchedEffect(pagerState.currentPage) {
        val category = categories[pagerState.currentPage]
        val newCategory = if (category == "All") null else category
        if (selectedCategory != newCategory) {
            viewModel.updateSelectedCategory(newCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar with Glass Effect - Editable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding() // Ensure it's not behind status bar
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = {
                    Text(
                        text = "Search Offers",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    }
                } else null,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Category Tabs - Custom Implementation with scroll indicator
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = true
            ) {
                items(categories.size) { index ->
                    val category = categories[index]
                    val isSelected = pagerState.currentPage == index
                    
                    Box(
                        modifier = Modifier
                            .height(40.dp) // Slightly more compact
                            .background(
                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = if (isSelected) Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                ) else Brush.horizontalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                Timber.tag("OfferListScreen").d("Tab clicked: $category (index: $index)")
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Reduced padding
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Visual indicator for more categories on the right
            if (categories.size > 3) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(40.dp)
                        .height(44.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Pager for Category Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true,
            beyondBoundsPageCount = 1
        ) { page ->
            // Offer List for current category
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show filtered offers or empty state
                if (offers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No offers found for ${categories[page]}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Try adjusting your search or filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = offers,
                        key = { _, offer -> offer.id }
                    ) { index, offer ->
                        // Trigger load more when reaching the end
                        if (index >= offers.lastIndex && !isLoadingMore && searchQuery.isEmpty()) {
                            LaunchedEffect(Unit) {
                                viewModel.loadMoreOffers()
                            }
                        }
                        
                        NeonOfferCard(
                            offer = offer,
                            onClick = { onOfferClick(offer.id) }
                        )
                    }
                    
                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00E4FF),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

