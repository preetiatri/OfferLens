package com.offerlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.offerlens.data.Offer
import com.offerlens.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.offerlens.R
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun NeonOfferCard(
    offer: Offer,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val offerDateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    val discountText = if (offer.discountType.equals("Percentage", ignoreCase = true)) {
        val value = if (offer.discountValue % 1.0 == 0.0) {
            offer.discountValue.toInt().toString()
        } else {
            offer.discountValue.toString()
        }
        "$value% OFF"
    } else {
        val value = if (offer.discountValue % 1.0 == 0.0) {
            offer.discountValue.toInt().toString()
        } else {
            offer.discountValue.toString()
        }
        "₹$value OFF"
    }
    
    // Null when the offer has no stated end date, in which case the line is omitted
    // rather than shown as "Expires Soon". Many bank offers simply don't publish an
    // expiry, and inventing urgency we can't back up misleads the user.
    val dateText: String? = try {
        offer.endDate?.let { context.getString(R.string.expires_prefix, offerDateFormatter.format(it.toDate())) }
    } catch (e: Exception) {
        null
    }
    
    // Determine color based on discount and theme
    val isDark = isSystemInDarkTheme()
    
    val baseColor = when {
        offer.discountValue >= 40 -> if (isDark) NeonCyan else CyanTeal
        offer.discountValue >= 25 -> if (isDark) NeonOrange else OrangeBurnt
        else -> if (isDark) NeonGreen else GreenEmerald
    }
    
    val neonColor = baseColor // Alias for clarity in existing code

    // Check if offer is "New" (created within last 48 hours)
    val isNew = remember(offer.createdAt) {
        val now = System.currentTimeMillis()
        val createdAt = offer.createdAt?.seconds?.times(1000) ?: 0L
        createdAt > 0 && (now - createdAt) < (48 * 60 * 60 * 1000)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Fixed height keeps the coupon button pinned to the card's bottom edge (the
            // inner Column relies on a weighted Spacer, which needs a bounded height).
            // Every element inside is single- or two-line capped, so content cannot
            // outgrow this - the clipping seen earlier came from the discount headline
            // wrapping, which is now prevented at source.
            .height(190.dp)
            .background(
                color = if (isDark) {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                } else {
                    Color.White // Solid white for light mode to show shadow/border clearly
                },
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp, // Thicker border
                brush = Brush.linearGradient(
                    colors = listOf(
                        neonColor,
                        Color.Transparent,
                        neonColor.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Merchant Logo (Using Coil or Fallback)
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                neonColor.copy(alpha = 0.2f),
                                DarkCardBackground // Use new color
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = neonColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                val faviconUrl = remember(offer.merchantUrl) {
                    if (offer.merchantUrl.isEmpty()) return@remember null
                    try {
                        val host = java.net.URI(offer.merchantUrl).host ?: ""
                        if (host.isNotEmpty()) "https://www.google.com/s2/favicons?domain=$host&sz=128" else null
                    } catch (e: Exception) {
                        null
                    }
                }
                var faviconFailed by remember(offer.merchantUrl) { mutableStateOf(false) }

                if (faviconUrl != null && !faviconFailed) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(faviconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        onState = { state ->
                            if (state is coil.compose.AsyncImagePainter.State.Error) {
                                faviconFailed = true
                            }
                        }
                    )
                }

                // Fallback initial/name shown whenever there's no usable favicon URL, or it failed to load.
                if (faviconUrl == null || faviconFailed) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = offer.merchant.take(1).uppercase(),
                            fontSize = 48.sp,
                            color = neonColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = offer.merchant,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            // NEW Badge Overlay
            if (isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .background(
                            color = if (isDark) NeonPinkAccent else PinkStrong, // Use new color
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = context.getString(R.string.new_badge),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top
            ) {
                Column {
                    // Same problem as the details screen at a smaller scale: the card's
                    // right column is narrow, so a five-digit amount wrapped and pushed
                    // the coupon button past the card's bottom edge.
                    Text(
                        text = discountText,
                        fontSize = when {
                            discountText.length <= 8 -> 28.sp
                            discountText.length <= 11 -> 22.sp
                            else -> 18.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = neonColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    // Show the cap on "X% off up to Rs Y" offers so the headline
                    // discount isn't misleading at a glance.
                    offer.maxDiscountAmount?.let { cap ->
                        if (cap > 0) {
                            val capText = if (cap % 1.0 == 0.0) cap.toInt().toString() else cap.toString()
                            Text(
                                text = "up to ₹$capText",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = offer.description.take(50) + if (offer.description.length > 50) "..." else "",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        lineHeight = 14.sp
                    )
                    dateText?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Copy Code Button
                val cleanCode = offer.couponCode.trim()
                val hasCode = cleanCode.isNotEmpty()
                // Bank portals often reveal the code only after login, so "No Code" would
                // wrongly suggest the discount applies without one.
                val codeOnSite = !hasCode && offer.couponRevealedOnSite
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(
                            color = if (hasCode) Color.Transparent else neonColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            width = 1.5.dp,
                            color = neonColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable(enabled = hasCode) {
                            if (hasCode) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Coupon Code", cleanCode)
                                clipboard.setPrimaryClip(clip)
                                
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.copied_toast, cleanCode),
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(
                                        if (codeOnSite) R.string.code_on_site_toast else R.string.no_code_toast
                                    ),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (hasCode) {
                            Text(
                                text = cleanCode,
                                color = neonColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = context.getString(R.string.cd_copy),
                                tint = neonColor,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = context.getString(
                                    if (codeOnSite) R.string.code_on_site_label else R.string.no_code_label
                                ),
                                color = neonColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
