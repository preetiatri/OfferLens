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
    private var mInterstitialAd: InterstitialAd? = null



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

    fun showInterstitial(activity: android.app.Activity) {
        // Check if user is premium
        if (premiumRepository.isPremium.value) {
            Timber.d("User is Premium. Skipping Interstitial Ad.")
            return
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        } else {
            Timber.d("The interstitial ad wasn't ready yet.")
            loadInterstitial()
        }
    }
}
