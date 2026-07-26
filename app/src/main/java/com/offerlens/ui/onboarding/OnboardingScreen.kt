package com.offerlens.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.offerlens.ui.theme.DeepBlack
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedBanks by remember { mutableStateOf(setOf<String>()) }
    var selectedPaymentTypes by remember { mutableStateOf(setOf<String>()) }

    val banks = com.offerlens.data.SmartWalletRepository.supportedBanks
    val paymentTypes = listOf("Credit Card", "Debit Card", "UPI", "Wallet")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Welcome Section
            Text(
                text = context.getString(com.offerlens.R.string.welcome_to),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OfferLens",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Discover the best offers on your cards",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Features List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem("🎯", "Personalized Offers", "Get offers tailored to your preferences")
                FeatureItem("💳", "All Payment Methods", "Credit cards, debit cards, UPI & wallets")
                FeatureItem("🔔", "Real-time Updates", "Never miss a great deal")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Payment Types Selection
            Text(
                text = "Which payment methods do you use?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                items(paymentTypes) { type ->
                    SelectableChip(
                        text = type,
                        isSelected = type in selectedPaymentTypes,
                        onClick = {
                            selectedPaymentTypes = if (type in selectedPaymentTypes) {
                                selectedPaymentTypes - type
                            } else {
                                selectedPaymentTypes + type
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bank Selection
            Text(
                text = "Which banks/cards do you use? (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Height derives from the list so the grid can't clip when issuers are added.
            // (Fixed height is required here: a lazy grid can't measure itself inside a
            // scrollable Column.) rows * chip height + (rows - 1) * spacing.
            val bankRows = (banks.size + 1) / 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height((bankRows * 48 + (bankRows - 1) * 8).dp)
            ) {
                items(banks) { bank ->
                    SelectableChip(
                        text = bank,
                        isSelected = bank in selectedBanks,
                        onClick = {
                            selectedBanks = if (bank in selectedBanks) {
                                selectedBanks - bank
                            } else {
                                selectedBanks + bank
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Legal Disclaimer
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
                    Text(
                        text = "⚠️",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "OfferLens is an independent, unofficial aggregator and is not affiliated with, endorsed by, or sponsored by any bank, card issuer, or merchant shown in the App. By continuing, you agree that all offer information is provided \"as is\" for informational purposes only. You must verify all offer details, terms, and conditions on the merchant's official website before purchase. OfferLens is not responsible for offer validity, cashback eligibility, missing rewards, or any transaction issues.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            Button(
                onClick = {
                    Timber.d("Continue button clicked")
                    
                    // Show toast to user
                    android.widget.Toast.makeText(
                        context,
                        context.getString(com.offerlens.R.string.signing_in_wait),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    
                    // Complete onboarding with user preferences
                    viewModel.completeOnboarding(
                        selectedBanks.toList(),
                        selectedPaymentTypes.toList(),
                        onComplete = {
                            Timber.d("onComplete called")
                            android.widget.Toast.makeText(
                                context,
                                context.getString(com.offerlens.R.string.welcome_back),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            onOnboardingComplete()
                        },
                        onError = { error ->
                            Timber.e(error, "Error: ${error.message}")
                            android.widget.Toast.makeText(
                                context,
                                context.getString(com.offerlens.R.string.error_saving_profile),
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(com.offerlens.R.string.get_started),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureItem(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                        else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
