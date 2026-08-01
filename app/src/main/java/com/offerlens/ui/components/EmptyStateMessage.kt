package com.offerlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offerlens.R
import com.offerlens.ui.theme.CyanTeal
import com.offerlens.ui.theme.NeonCyan

@Composable
fun EmptyStateMessage(
    category: String,
    isSearchActive: Boolean,
    isSmartWalletActive: Boolean = false,
    onDisableFilter: (() -> Unit)? = null
) {
    // The Smart Wallet filter empties far more category/issuer combinations than it fills,
    // so blaming the category ("check back later") would misattribute the cause and hide
    // the single toggle that fixes it.
    val blamesFilter = isSmartWalletActive && !isSearchActive

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                isSearchActive -> stringResource(R.string.no_results)
                blamesFilter -> stringResource(R.string.no_wallet_matches)
                else -> stringResource(R.string.no_offers_yet)
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = when {
                isSearchActive -> stringResource(R.string.search_suggestion, category)
                blamesFilter -> stringResource(R.string.wallet_filter_hint, category)
                else -> stringResource(R.string.check_back_later, category)
            },
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Offered whenever the filter is on, including during a search, since it may be
        // the filter rather than the search term that emptied the list.
        if (isSmartWalletActive && onDisableFilter != null) {
            val accent = if (isSystemInDarkTheme()) NeonCyan else CyanTeal
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .background(
                        color = accent.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = accent,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onDisableFilter)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.show_all_offers),
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
