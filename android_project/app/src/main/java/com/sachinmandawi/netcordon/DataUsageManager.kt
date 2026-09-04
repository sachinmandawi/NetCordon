package com.sachinmandawi.netcordon

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import java.util.Calendar
import java.util.Locale

data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val uid: Int,
    val rxWifi: Long,
    val txWifi: Long,
    val rxMobile: Long,
    val txMobile: Long,
    val isWifiBlocked: Boolean = false,
    val isDataBlocked: Boolean = false
) {
    val totalWifi: Long get() = rxWifi + txWifi
    val totalMobile: Long get() = rxMobile + txMobile
    val totalBytes: Long get() = totalWifi + totalMobile
}

data class OverallNetworkStats(
    val totalWifi: Long,
    val totalMobile: Long,
    val totalSavedBytes: Long,
    val blockedPingsCount: Int,
    val appStats: List<AppUsageStats>
) {
    val totalBytes: Long get() = totalWifi + totalMobile
}

object DataUsageManager {
    private const val TAG = "DataUsageManager"

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
            bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
            bytes >= kb -> String.format(Locale.US, "%.1f KB", bytes / kb)
            else -> "$bytes B"
        }
    }

    fun getTimeRange(period: String): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = when (period.lowercase()) {
            "week" -> now - (7L * 24 * 60 * 60 * 1000)
            "month" -> now - (30L * 24 * 60 * 60 * 1000)
            else -> calendar.timeInMillis // "today"
        }
        return Pair(startTime, now)
    }

    @Suppress("DEPRECATION")
    fun getUidStats(
        context: Context,
        uid: Int,
        startTime: Long,
        endTime: Long
    ): Quad<Long, Long, Long, Long> {
        var rxWifi = 0L
        var txWifi = 0L
        var rxMobile = 0L
        var txMobile = 0L

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Quad(0L, 0L, 0L, 0L)
        }

        try {
            val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                ?: return Quad(0L, 0L, 0L, 0L)

            // Query WiFi stats
            try {
                val wifiStats = nsm.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    null,
                    startTime,
                    endTime,
                    uid
                )
                try {
                    val bucket = NetworkStats.Bucket()
                    while (wifiStats.hasNextBucket()) {
                        wifiStats.getNextBucket(bucket)
                        rxWifi += bucket.rxBytes
                        txWifi += bucket.txBytes
                    }
                } finally {
                    wifiStats.close()
                }
            } catch (e: Exception) {
                // WiFi stats might fail or be empty for this UID
            }

            // Query Mobile Data stats
            try {
                val mobileStats = nsm.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    null,
                    startTime,
                    endTime,
                    uid
                )
                try {
                    val bucket = NetworkStats.Bucket()
                    while (mobileStats.hasNextBucket()) {
                        mobileStats.getNextBucket(bucket)
                        rxMobile += bucket.rxBytes
                        txMobile += bucket.txBytes
                    }
                } finally {
                    mobileStats.close()
                }
            } catch (e: Exception) {
                // Mobile stats might fail or be empty
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to query network stats for UID $uid", e)
        }

        return Quad(rxWifi, txWifi, rxMobile, txMobile)
    }

    @Suppress("DEPRECATION")
    fun getOverallStats(
        context: Context,
        apps: List<AppInfo>,
        period: String = "today"
    ): OverallNetworkStats {
        val (startTime, endTime) = getTimeRange(period)
        val statsList = mutableListOf<AppUsageStats>()

        var aggregateWifi = 0L
        var aggregateMobile = 0L

        val uidUsageMap = mutableMapOf<Int, LongArray>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                if (nsm != null) {
                    try {
                        val wifiStats = nsm.queryDetails(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
                        try {
                            val bucket = NetworkStats.Bucket()
                            while (wifiStats.hasNextBucket()) {
                                wifiStats.getNextBucket(bucket)
                                val arr = uidUsageMap.getOrPut(bucket.uid) { LongArray(4) }
                                arr[0] += bucket.rxBytes
                                arr[1] += bucket.txBytes
                            }
                        } finally {
                            wifiStats.close()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "WiFi queryDetails failed: ${e.message}")
                    }

                    try {
                        val mobileStats = nsm.queryDetails(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
                        try {
                            val bucket = NetworkStats.Bucket()
                            while (mobileStats.hasNextBucket()) {
                                mobileStats.getNextBucket(bucket)
                                val arr = uidUsageMap.getOrPut(bucket.uid) { LongArray(4) }
                                arr[2] += bucket.rxBytes
                                arr[3] += bucket.txBytes
                            }
                        } finally {
                            mobileStats.close()
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Mobile queryDetails failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in batch queryDetails", e)
            }

            // Fallback: If device-wide queryDetails returned nothing (OEM permission restriction),
            // query per-UID directly for installed apps
            if (uidUsageMap.isEmpty()) {
                for (app in apps) {
                    val (rxW, txW, rxM, txM) = getUidStats(context, app.uid, startTime, endTime)
                    if (rxW + txW + rxM + txM > 0) {
                        uidUsageMap[app.uid] = longArrayOf(rxW, txW, rxM, txM)
                    }
                }
            }
        }

        for (app in apps) {
            val usageArr = uidUsageMap[app.uid] ?: LongArray(4)
            val rxW = usageArr[0]
            val txW = usageArr[1]
            val rxM = usageArr[2]
            val txM = usageArr[3]

            val isAppRestricted = app.wifiBlocked || app.dataBlocked || app.isBlackout || app.isSmartShield
            val appUsage = AppUsageStats(
                packageName = app.packageName,
                appName = app.appName,
                uid = app.uid,
                rxWifi = rxW,
                txWifi = txW,
                rxMobile = rxM,
                txMobile = txM,
                isWifiBlocked = app.wifiBlocked,
                isDataBlocked = app.dataBlocked
            )
            aggregateWifi += (rxW + txW)
            aggregateMobile += (rxM + txM)

            if (appUsage.totalBytes > 0 || isAppRestricted) {
                statsList.add(appUsage)
            }
        }

        // Sort by total data consumed descending
        statsList.sortByDescending { it.totalBytes }

        // Calculate estimated background data saved by blocked apps (including Smart Shield)
        val blockedApps = apps.filter { it.wifiBlocked || it.dataBlocked || it.isBlackout || it.isSmartShield }
        val blockedCount = blockedApps.size
        
        // Estimated savings based on restricted background drain & real caught leak attempts
        var estimatedSavedBytes = 0L
        val leakAttemptsMap = PrefsManager.getTodayBlockedAttempts(context)
        for (blocked in blockedApps) {
            val usage = statsList.firstOrNull { it.uid == blocked.uid }
            val baseUsage = usage?.totalBytes ?: 0L
            val appLeakAttempts = leakAttemptsMap[blocked.packageName] ?: 0
            val estimatedAppSaved = if (baseUsage > 0) {
                (baseUsage * 0.25).toLong()
            } else if (appLeakAttempts > 0) {
                appLeakAttempts * 3L * 1024 * 1024
            } else {
                12L * 1024 * 1024
            }
            estimatedSavedBytes += estimatedAppSaved
        }

        // Calculate blocked background pings: use real detected leak attempts if available
        val realBlockedAttempts = PrefsManager.getTodayTotalBlockedAttempts(context)
        val hoursElapsed = maxOf(1L, (endTime - startTime) / (3600 * 1000))
        val blockedPings = if (realBlockedAttempts > 0) {
            realBlockedAttempts
        } else {
            (blockedCount * hoursElapsed * 18).toInt()
        }

        return OverallNetworkStats(
            totalWifi = aggregateWifi,
            totalMobile = aggregateMobile,
            totalSavedBytes = estimatedSavedBytes,
            blockedPingsCount = blockedPings,
            appStats = statsList
        )
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
