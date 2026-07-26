package com.offerlens.data

import android.content.Context
import timber.log.Timber
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val premiumRepository: PremiumRepository
) {
    private companion object {
        /**
         * Don't interrupt the user on their first few offers. An interstitial on the
         * very first tap reads as hostile and is the pattern AdMob's policies treat
         * as disruptive.
         */
        const val MIN_OFFER_OPENS_BEFORE_AD = 3

        /** A gap this long counts as a new session, so a returning user is eligible again. */
        const val SESSION_TIMEOUT_MS = 30L * 60L * 1000L
    }

    private var mInterstitialAd: InterstitialAd? = null

    // Frequency-capping state. AdManager is a @Singleton, so this persists for the
    // life of the process - i.e. one interstitial per app session, not per tap.
    private var offerOpenCount = 0
    private var shownThisSession = false
    private var lastInteractionAt = 0L

    init {
        MobileAds.initialize(context) { }
        loadInterstitial()
    }

    fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            com.offerlens.BuildConfig.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.d(adError.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Timber.d("Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Timber.d("Ad dismissed fullscreen content.")
                            mInterstitialAd = null
                            loadInterstitial() // Reload for next time
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Timber.e("Ad failed to show fullscreen content.")
                            mInterstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Timber.d("Ad showed fullscreen content.")
                            // mInterstitialAd = null // Do not nullify here, wait for dismiss
                        }
                    }
                }
            })
    }

    /**
     * Called when the user opens an offer. Shows at most ONE interstitial per session,
     * and only after they've opened a few offers.
     *
     * Previously this fired on every single offer tap, which interrupted the user
     * mid-navigation on every interaction. That risks an AdMob policy strike for
     * disruptive placement, and is bad economics: an interstitial impression is worth
     * a tiny fraction of the affiliate commission earned when a user actually follows
     * an offer through to the merchant.
     */
    fun showInterstitial(activity: android.app.Activity) {
        if (premiumRepository.isPremium.value) {
            Timber.d("User is Premium. Skipping Interstitial Ad.")
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastInteractionAt > SESSION_TIMEOUT_MS) {
            // Returning after a long gap - treat as a fresh session.
            shownThisSession = false
            offerOpenCount = 0
        }
        lastInteractionAt = now
        offerOpenCount++

        if (shownThisSession) {
            Timber.d("Interstitial already shown this session. Skipping.")
            return
        }
        if (offerOpenCount < MIN_OFFER_OPENS_BEFORE_AD) {
            Timber.d("Only $offerOpenCount offer(s) opened this session. Skipping interstitial.")
            return
        }

        val ad = mInterstitialAd
        if (ad != null) {
            // Mark before showing so a failure to display doesn't retry on the next tap.
            shownThisSession = true
            ad.show(activity)
        } else {
            Timber.d("The interstitial ad wasn't ready yet.")
            loadInterstitial()
        }
    }
}
