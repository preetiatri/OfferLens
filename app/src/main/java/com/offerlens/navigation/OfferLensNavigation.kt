package com.offerlens.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.offerlens.ui.home.NeoGlassmorphicHomeScreen
import com.offerlens.ui.onboarding.OnboardingScreen
import com.offerlens.ui.settings.AboutScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OfferLensNavigation() {
    val navController = rememberNavController()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "home" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            // AdManager is a @Singleton @Inject class, we can just get it if it's in the AppModule
            // but for Compose, it's easier to access via ViewModel or a local CompositionLocal if provided.
            // For now, satisfy the constructor.
            NeoGlassmorphicHomeScreen(
                onOfferClick = { offerId ->
                    navController.navigate("offer_details/$offerId")
                },
                onViewAllOffers = {
                    navController.navigate("premium")
                },
                onCalculatorClick = {
                    navController.navigate("calculator")
                },
                onAboutClick = {
                    navController.navigate("about")
                },
                onDataDeleted = {
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("about") {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("calculator") {
            com.offerlens.ui.calculator.CalculatorScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("premium") {
            com.offerlens.ui.premium.PremiumScreen(
                onBackClick = { navController.popBackStack() },
                onManageCardsClick = { navController.navigate("my_cards") }
            )
        }

        composable("my_cards") {
            com.offerlens.ui.wallet.MyCardsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
// ...
        
        composable(
            route = "offer_details/{offerId}",
            arguments = listOf(androidx.navigation.navArgument("offerId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: return@composable
            com.offerlens.ui.offer.OfferDetailsScreen(
                offerId = offerId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
