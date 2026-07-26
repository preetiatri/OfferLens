package com.offerlens.ui.offer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.offerlens.ui.theme.ContentCopy
import androidx.compose.material.icons.filled.Info
import com.offerlens.ui.theme.OpenInNew
import com.offerlens.ui.theme.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.offerlens.data.Offer
import com.offerlens.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OfferDetailsScreen(
    offerId: String,
    onBackClick: () -> Unit,
    viewModel: OfferDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val offer by viewModel.offer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // Error state reflects the ViewModel's actual load outcome, not a fixed timer -
    // avoids flashing a false "failed to load" message while a slow request is still in flight.
    val showError = !isLoading && offer == null

    LaunchedEffect(offerId) {
        viewModel.loadOffer(offerId)
    }

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
                            (if (isSystemInDarkTheme()) NeonCyan else CyanTeal).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(500f, 0f),
                        radius = 1000f
                    )
                )
        )

        if (offer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showError) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Failed to load offer",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Please check your connection and try again",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSystemInDarkTheme()) NeonCyan else CyanTeal)
                        ) {
                            Text("Go Back", color = Color.Black)
                        }
                    }
                } else {
                    CircularProgressIndicator(color = if (isSystemInDarkTheme()) NeonCyan else CyanTeal)
                }
            }
        } else {
            val currentOffer = offer!!
            val isDark = isSystemInDarkTheme()
            val neonColor = when {
                currentOffer.discountValue >= 40 -> if (isDark) NeonCyan else CyanTeal
                currentOffer.discountValue >= 25 -> if (isDark) NeonOrange else OrangeBurnt
                else -> if (isDark) NeonGreen else GreenEmerald
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Offer Details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Merchant Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        neonColor.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, neonColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentOffer.merchant.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = neonColor
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = currentOffer.merchant,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentOffer.category,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Discount Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(neonColor.copy(alpha = 0.5f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "OFFER VALUE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val discountText = if (currentOffer.discountType.equals("Percentage", ignoreCase = true)) {
                            val value = if (currentOffer.discountValue % 1.0 == 0.0) {
                                currentOffer.discountValue.toInt().toString()
                            } else {
                                currentOffer.discountValue.toString()
                            }
                            "$value% OFF"
                        } else {
                            val value = if (currentOffer.discountValue % 1.0 == 0.0) {
                                currentOffer.discountValue.toInt().toString()
                            } else {
                                currentOffer.discountValue.toString()
                            }
                            "₹$value OFF"
                        }
                        
                        Text(
                            text = discountText,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = neonColor
                        )
                        // Most bank offers are "X% off up to Rs Y" - without the cap the
                        // headline overstates what the user actually saves.
                        currentOffer.maxDiscountAmount?.let { cap ->
                            if (cap > 0) {
                                val capText = if (cap % 1.0 == 0.0) cap.toInt().toString() else cap.toString()
                                Text(
                                    text = "up to ₹$capText",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentOffer.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Per-product breakdown for bundled offers. A headline like "up to Rs 5000"
                // is misleading on its own when the cap and minimum spend differ per
                // product, so show the tier that actually applies to the user's purchase.
                if (currentOffer.tiers.isNotEmpty()) {
                    Text(
                        text = "WHAT YOU GET",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = neonColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        currentOffer.tiers.forEachIndexed { index, tier ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tier.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val conditions = buildList {
                                        tier.minOrderValue?.let { if (it > 0) add("min order ₹${it.toInt()}") }
                                        if (tier.note.isNotBlank()) add(tier.note)
                                    }
                                    if (conditions.isNotEmpty()) {
                                        Text(
                                            text = conditions.joinToString(" · "),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tier.maxDiscountAmount?.let { "up to ₹${it.toInt()}" }
                                        ?: if (currentOffer.discountType.equals("Percentage", ignoreCase = true))
                                            "${tier.discountValue.toInt()}%"
                                        else "₹${tier.discountValue.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = neonColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Coupon Code Section - Only show if not empty after trim
                if (currentOffer.couponCode.trim().isNotEmpty()) {
                    Text(
                        text = "COUPON CODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Minimum rather than fixed, so a two-line code isn't clipped.
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Coupon Code", currentOffer.couponCode.trim())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // This is the screen users come to in order to READ the code, so
                            // it must never be cut off. Long codes step down in size and drop
                            // the letter spacing rather than ellipsising, and may wrap to a
                            // second line.
                            val code = currentOffer.couponCode.trim()
                            val codeStyle = when {
                                code.length <= 14 -> Triple(20.sp, 2.sp, 1)
                                code.length <= 22 -> Triple(16.sp, 1.sp, 1)
                                else -> Triple(14.sp, 0.sp, 2)
                            }
                            Text(
                                text = code,
                                fontSize = codeStyle.first,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = codeStyle.second,
                                maxLines = codeStyle.third,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = neonColor
                            )
                        }
                    }
                } else if (currentOffer.couponRevealedOnSite) {
                    // A code IS needed, it just isn't published here. Say so, so the user
                    // doesn't click through expecting the discount to apply automatically.
                    Text(
                        text = "COUPON CODE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = neonColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            // Sets expectations before the redirect: bank portals (e.g. Axis
                            // Grab Deals) ask the user to verify their registered mobile and
                            // card digits before revealing the code. Unexplained, that screen
                            // looks like a scam and users abandon the offer. The closing line
                            // is also an anti-phishing signal - OfferLens never asks for these,
                            // so an app that does is easier to recognise as fake.
                            text = "This offer needs a coupon code from the bank's site. You may be asked to " +
                                "verify your registered mobile number and card details on the bank's official " +
                                "page before the code is shown. That happens on the bank's own site — OfferLens " +
                                "never asks for your card details.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Details Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailItem(
                        label = "Bank",
                        value = currentOffer.bankName,
                        modifier = Modifier.weight(1f)
                    )
                    DetailItem(
                        label = "Payment",
                        value = currentOffer.paymentType,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                    // Say plainly that no expiry was published rather than "N/A", which
                    // reads like missing data instead of a fact about the offer.
                    val dateStr = currentOffer.endDate?.let { formatter.format(it.toDate()) }
                        ?: "Not stated"

                    DetailItem(
                        label = "Valid Until",
                        value = dateStr,
                        modifier = Modifier.weight(1f)
                    )
                    // A tiered offer's minimum varies per product, so a single figure here
                    // would be wrong - the breakdown above carries the real numbers.
                    DetailItem(
                        label = "Min. Order",
                        value = when {
                            currentOffer.tiers.isNotEmpty() -> "See breakdown"
                            (currentOffer.minOrderValue ?: 0.0) > 0 -> "₹${currentOffer.minOrderValue!!.toInt()}"
                            else -> "None"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // External Links.
                //
                // For portal offers (Axis Grab Deals and similar) the discount only applies
                // if the user STARTS at the bank's page and authenticates there - going
                // straight to the merchant silently forfeits it. Leading with "Visit
                // <merchant>" in that case would send users past the offer while our
                // affiliate link still fired, i.e. we'd earn on a click that cost the user
                // the very discount we advertised. So when a code is only issued on the
                // bank's site, the source link is promoted to primary and the direct
                // merchant link is labelled as not carrying the offer.
                val startAtSource = currentOffer.couponRevealedOnSite &&
                    currentOffer.offerSourceUrl.isNotEmpty()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (startAtSource) {
                        Button(
                            onClick = {
                                com.offerlens.data.affiliate.AffiliateManager.openOfferLink(context, currentOffer, useSourceUrl = true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonColor),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentOffer.bankName.isNotBlank())
                                        "Start at ${currentOffer.bankName}" else "Start at bank page",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "This offer must be started from the bank's page to apply.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (currentOffer.merchantUrl.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    com.offerlens.data.affiliate.AffiliateManager.openOfferLink(context, currentOffer, useSourceUrl = false)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "Go to ${currentOffer.merchant} without this offer",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // 1. Visit Merchant Button (Primary)
                        if (currentOffer.merchantUrl.isNotEmpty()) {
                            Button(
                                onClick = {
                                    com.offerlens.data.affiliate.AffiliateManager.openOfferLink(context, currentOffer, useSourceUrl = false)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = neonColor),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Visit ${currentOffer.merchant}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White // Always white on Primary/Neon button
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // 2. View Offer Source (Secondary)
                        if (currentOffer.offerSourceUrl.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    com.offerlens.data.affiliate.AffiliateManager.openOfferLink(context, currentOffer, useSourceUrl = true)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "View Offer Source (e.g. Bank Page)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                // Terms & Conditions Section (only shown when the offer actually has T&C text)
                if (currentOffer.termsAndConditions.isNotEmpty()) {
                    var isExpanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TERMS & CONDITIONS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isExpanded) "−" else "+",
                                fontSize = 24.sp,
                                color = neonColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentOffer.termsAndConditions,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Disclaimer - always shown regardless of whether this offer has T&C text,
                // since the risk (unverified offer, no OfferLens liability) is the same either way.
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = (if (isSystemInDarkTheme()) NeonOrange else OrangeBurnt).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️ IMPORTANT: Please verify this offer on ${currentOffer.merchant}'s official website before making a purchase. Offer details, terms, and conditions may change without notice. OfferLens is an independent aggregator, not affiliated with or endorsed by ${currentOffer.bankName.ifEmpty { "the issuing bank" }} or ${currentOffer.merchant}, and is not responsible for offer validity, cashback eligibility, missing rewards, or any transaction issues.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}
