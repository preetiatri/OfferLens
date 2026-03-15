package com.offerlens.ui

sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object OfferList : Screen("offer_list")
    object OfferDetail : Screen("offer_detail/{offerId}") {
        fun createRoute(offerId: String) = "offer_detail/$offerId"
    }
}
