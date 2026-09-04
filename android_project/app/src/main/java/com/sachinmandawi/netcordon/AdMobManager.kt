package com.sachinmandawi.netcordon

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Collections

object AdMobManager {
    private const val TAG = "AdMobManager"

    // =========================================================================
    // >>> ADMOB CONFIGURATION: TEST MODE VS PRODUCTION <<<
    // Set to true to test right now with 100% instant fill!
    // Set to false when publishing for real users and real revenue.
    // =========================================================================
    const val USE_TEST_ADS = true

    // Your Production IDs:
    const val PROD_REWARDED_ID     = "ca-app-pub-9886575848738700/1287079746"
    const val PROD_BANNER_ID       = "ca-app-pub-9886575848738700/2051215704"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-9886575848738700/7896614925"

    // Google Official Sample Test IDs (Guaranteed instant load for testing):
    const val TEST_REWARDED_ID     = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_BANNER_ID       = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    val REWARDED_AD_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_REWARDED_ID else PROD_REWARDED_ID

    val BANNER_AD_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_BANNER_ID else PROD_BANNER_ID

    val INTERSTITIAL_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
    // =========================================================================

    private var isInitialized = false
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false
    private val pendingRewardedCallbacks = Collections.synchronizedList(mutableListOf<() -> Unit>())

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private val pendingInterstitialCallbacks = Collections.synchronizedList(mutableListOf<() -> Unit>())

    /**
     * Initialize MobileAds SDK and pre-load initial ads.
     */
    fun init(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initStatus ->
                Log.d(TAG, "AdMob MobileAds initialized: $initStatus")
                isInitialized = true
                // Pre-load ads on initialization
                loadRewardedAd(context.applicationContext)
                loadInterstitialAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
        }
    }

    /* ── Rewarded Video Ads (24h VIP Pass) ── */

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun loadRewardedAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (rewardedAd != null) {
            onLoaded?.invoke()
            return
        }

        if (onLoaded != null) {
            pendingRewardedCallbacks.add(onLoaded)
        }

        if (isRewardedLoading) {
            return
        }

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                    rewardedAd = ad
                    isRewardedLoading = false
                    val callbacks = synchronized(pendingRewardedCallbacks) {
                        val copy = ArrayList(pendingRewardedCallbacks)
                        pendingRewardedCallbacks.clear()
                        copy
                    }
                    callbacks.forEach { it.invoke() }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    isRewardedLoading = false
                    val callbacks = synchronized(pendingRewardedCallbacks) {
                        val copy = ArrayList(pendingRewardedCallbacks)
                        pendingRewardedCallbacks.clear()
                        copy
                    }
                    callbacks.forEach { it.invoke() }
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onAdFailed("Activity is no longer active.")
            return
        }

        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewardedAd(activity.applicationContext)
                    onAdFailed(adError.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad displayed.")
                }
            }
            currentAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                activity.runOnUiThread {
                    onUserEarnedReward(rewardItem)
                }
            }
        } else {
            loadRewardedAd(activity.applicationContext) {
                activity.runOnUiThread {
                    if (activity.isFinishing || activity.isDestroyed) {
                        onAdFailed("Activity was closed while loading ad.")
                        return@runOnUiThread
                    }
                    val freshlyLoaded = rewardedAd
                    if (freshlyLoaded != null) {
                        freshlyLoaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Rewarded ad dismissed.")
                                rewardedAd = null
                                loadRewardedAd(activity.applicationContext)
                                onAdDismissed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                                rewardedAd = null
                                loadRewardedAd(activity.applicationContext)
                                onAdFailed(adError.message)
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Rewarded ad displayed.")
                            }
                        }
                        freshlyLoaded.show(activity) { rewardItem ->
                            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                            activity.runOnUiThread {
                                onUserEarnedReward(rewardItem)
                            }
                        }
                    } else {
                        onAdFailed("Ad failed to load. Please check internet connection.")
                    }
                }
            }
        }
    }

    /* ── Interstitial Ads (Action Transitions) ── */

    fun isInterstitialAdReady(): Boolean = interstitialAd != null

    fun loadInterstitialAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (interstitialAd != null) {
            onLoaded?.invoke()
            return
        }

        if (onLoaded != null) {
            pendingInterstitialCallbacks.add(onLoaded)
        }

        if (isInterstitialLoading) {
            return
        }

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isInterstitialLoading = false
                    val callbacks = synchronized(pendingInterstitialCallbacks) {
                        val copy = ArrayList(pendingInterstitialCallbacks)
                        pendingInterstitialCallbacks.clear()
                        copy
                    }
                    callbacks.forEach { it.invoke() }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                    val callbacks = synchronized(pendingInterstitialCallbacks) {
                        val copy = ArrayList(pendingInterstitialCallbacks)
                        pendingInterstitialCallbacks.clear()
                        copy
                    }
                    callbacks.forEach { it.invoke() }
                }
            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        force: Boolean = false,
        onDismissed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onDismissed()
            return
        }

        if (!force && PrefsManager.isAdFreeActive(activity)) {
            onDismissed()
            return
        }

        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    PrefsManager.recordInterstitialShown(activity)
                    loadInterstitialAd(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad displayed.")
                    PrefsManager.recordInterstitialShown(activity)
                }
            }
            currentAd.show(activity)
        } else {
            // Ad not ready in memory yet: fetch and show immediately once ready
            loadInterstitialAd(activity.applicationContext) {
                activity.runOnUiThread {
                    if (activity.isFinishing || activity.isDestroyed) {
                        onDismissed()
                        return@runOnUiThread
                    }
                    val freshlyLoaded = interstitialAd
                    if (freshlyLoaded != null) {
                        freshlyLoaded.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial ad dismissed.")
                                interstitialAd = null
                                PrefsManager.recordInterstitialShown(activity)
                                loadInterstitialAd(activity.applicationContext)
                                onDismissed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                                interstitialAd = null
                                loadInterstitialAd(activity.applicationContext)
                                onDismissed()
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Interstitial ad displayed.")
                                PrefsManager.recordInterstitialShown(activity)
                            }
                        }
                        freshlyLoaded.show(activity)
                    } else {
                        onDismissed()
                    }
                }
            }
        }
    }

    /* ── Jetpack Compose Banner Ad View ── */

    @Composable
    fun BannerAdView(
        modifier: Modifier = Modifier
    ) {
        val theme = LocalThemeColors.current
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .background(theme.surf, shape = RoundedCornerShape(12.dp))
                .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BANNER_AD_UNIT_ID
                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdLoaded() {
                                Log.d(TAG, "Banner ad loaded successfully.")
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.w(TAG, "Banner ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    try {
                        adView.destroy()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error destroying AdView", e)
                    }
                }
            )
        }
    }
}
