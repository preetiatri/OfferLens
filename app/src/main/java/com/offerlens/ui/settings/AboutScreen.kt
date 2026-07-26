package com.offerlens.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offerlens.ui.theme.*
import com.offerlens.ui.theme.NeonCyan
import com.offerlens.ui.theme.CyanTeal
import com.offerlens.ui.theme.NeonGreen
import com.offerlens.ui.theme.GreenEmerald

@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "About OfferLens",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) NeonCyan else CyanTeal,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "OfferLens",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Smart Offer Discovery Platform",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Disclaimer
            Text(
                text = "LEGAL DISCLAIMER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) NeonOrange else MaterialTheme.colorScheme.error, // Use darker color for light mode
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NOT AFFILIATED WITH ANY BANK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) NeonOrange else MaterialTheme.colorScheme.error,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "OfferLens is an independent, unofficial aggregator and is not affiliated with, endorsed by, or sponsored by any bank, card issuer, or merchant shown in the App. All bank and merchant names, logos, and trademarks belong to their respective owners.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO LIABILITY FOR OFFER VALIDITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) NeonOrange else MaterialTheme.colorScheme.error,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "OfferLens aggregates offer information curated by our team. We make reasonable efforts to ensure accuracy, but offers may change, expire, or be discontinued by the merchant without notice.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "VERIFY BEFORE PURCHASE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You MUST verify all offer details, cashback terms, and redemption conditions on the merchant's or bank's official website before making any purchase. OfferLens is NOT responsible for:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Invalid or expired coupons/deals\n• Cashback or rewards not tracking or being rejected\n• Incorrect discount values or minimum order requirements\n• Direct financial loss from merchant transactions\n• Merchant's refusal to honor an aggregated offer",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LEGAL SAFEGUARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "By using OfferLens, you agree that the platform and its developers are NOT liable for any claims resulting from offer usage. All data is provided \"AS IS\" for informational purposes only. This app is compliant with India's DPDP Act 2023 regarding user data protection.",
                        fontSize = 13.sp,
                        color = if (isSystemInDarkTheme()) NeonOrange.copy(alpha = 0.95f) else MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How We Make Money - shown in-app rather than only in the Terms, since a user
            // deciding whether to trust a recommendation shouldn't have to open a web page
            // to find out whether we're paid for it.
            Text(
                text = "HOW WE MAKE MONEY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) NeonCyan else CyanTeal,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Some links in this app are affiliate links. If you buy after following one, we may earn a commission — at no extra cost to you. The price you pay is the same either way.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• We rank offers by value to you, not by what pays us\n• We're not paid by any bank to list their offers\n• Many offers earn us nothing — where an offer must be started on a bank's page, the bank gets the commission, and we still send you there so you keep the discount\n• We also earn from ads and optional Premium",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Sources
            Text(
                text = "DATA SOURCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) NeonCyan else CyanTeal,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DataSourceItem("Manual Curation", "Offers are reviewed and entered by our team from publicly available sources")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy
            Text(
                text = "PRIVACY & DATA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) NeonGreen else GreenEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "• We do not collect personal financial information",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• User submissions are voluntary and attributed",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• We comply with India's DPDP Act 2023",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "• Your data is securely stored on Firebase",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mandatory Privacy Policy Link
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Text(
                        text = "View Privacy Policy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) NeonCyan else CyanTeal,
                        modifier = Modifier
                            .  clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://offerlens.asadigital.co.in/privacy/offerlens.html"))
                                context.startActivity(intent)
                            },
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "Made with ❤️ in India",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DataSourceItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
