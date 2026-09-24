package com.sachinmandawi.netcordon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class AppShieldService : Service() {

    private lateinit var workerThread: HandlerThread
    private lateinit var handler: Handler
    @Volatile private var isRunning = false
    @Volatile private var lastForegroundPackage = ""
    private val shieldedPackages: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()
    private val packageUidMap = java.util.concurrent.ConcurrentHashMap<String, Int>()

    private val checkForegroundRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val currentApp = getForegroundPackageName()
            if (currentApp.isNotEmpty() && currentApp != lastForegroundPackage) {
                Log.d(TAG, "Foreground App Changed: $lastForegroundPackage -> $currentApp")
                val prevApp = lastForegroundPackage
                lastForegroundPackage = currentApp
                onForegroundAppChanged(prevApp, currentApp)
            }

            checkBackgroundLeaks()
            checkDailyQuotas()
            handler.postDelayed(this, 500) // Poll on worker thread
        }
    }

    private var leakCheckTicks = 0

    private fun getAppLabel(packageName: String): String {
        return try {
            val pm = packageManager
            val ai = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(ai).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
    }

    private fun checkBackgroundLeaks() {
        leakCheckTicks++
        // Check every 30 seconds (60 ticks of 500ms)
        if (leakCheckTicks < 60) return
        leakCheckTicks = 0

        if (!PrefsManager.isLeakAlertsEnabled(this)) return

        val threshold = PrefsManager.getLeakAlertThreshold(this)
        val fg = lastForegroundPackage

        // Detect packages that actually attempted background wake/service activity in the last 30s
        val activeBgPackages = mutableSetOf<String>()
        try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usm != null) {
                val now = System.currentTimeMillis()
                val events = usm.queryEvents(now - 30_000L, now)
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.packageName != fg && shieldedPackages.contains(event.packageName)) {
                        activeBgPackages.add(event.packageName)
                    }
                }
            }
        } catch (_: Exception) {
        }

        for (pkg in activeBgPackages) {
            if (pkg == fg) continue
            PrefsManager.recordBlockedAttempt(this, pkg, 1)

            val count = PrefsManager.getTodayBlockedAttempts(this)[pkg] ?: 0
            if (count >= threshold && count % threshold == 0) {
                dispatchLeakNotification(pkg, count)
            }
        }
    }

    private fun dispatchLeakNotification(packageName: String, attempts: Int) {
        val appName = getAppLabel(packageName)

        val blackoutIntent = Intent(this, AppShieldService::class.java).apply {
            action = ACTION_QUICK_BLACKOUT
            putExtra(EXTRA_PACKAGE, packageName)
        }
        val blackoutPI = PendingIntent.getService(
            this,
            packageName.hashCode(),
            blackoutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AppShieldService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_PACKAGE, packageName)
        }
        val dismissPI = PendingIntent.getService(
            this,
            (packageName + "_dismiss").hashCode(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppPI = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, LEAK_CHANNEL_ID)
            .setContentTitle("🚨 Background Data Leak: $appName")
            .setContentText("Attempted $attempts background connections while closed. Tap to isolate.")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFFF5252.toInt())
            .setContentIntent(openAppPI)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "🔴 Total Blackout", blackoutPI)
            .addAction(0, "✕ Dismiss", dismissPI)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(packageName.hashCode(), notification)
    }

    private var quotaCheckTicks = 0

    private fun checkDailyQuotas() {
        quotaCheckTicks++
        // Check every 10 seconds (20 ticks of 500ms)
        if (quotaCheckTicks < 20) return
        quotaCheckTicks = 0

        // 1. Midnight Auto-Reset: Unblock apps whose daily quota expired yesterday
        val unblocked = PrefsManager.checkAndResetQuotaDaily(this)
        if (unblocked.isNotEmpty()) {
            Log.d(TAG, "Midnight reset: Unblocking ${unblocked.size} apps that hit quota yesterday")
            val savedBlackout = PrefsManager.getBlackoutPackages(this)
            val rules = mutableListOf<ShizukuManager.BatchAppRule>()
            for (pkg in unblocked) {
                val uid = packageUidMap[pkg] ?: PrefsManager.getPackageUidMap(this)[pkg] ?: continue
                if (!savedBlackout.contains(pkg)) {
                    val mode = PrefsManager.getAppIsolationMode(this, pkg)
                    rules.add(ShizukuManager.BatchAppRule(pkg, uid, mode, isForeground = false))
                }
            }
            if (rules.isNotEmpty()) {
                ShizukuManager.applyBatchIsolationRules(rules, blockNotifications = false)
            }
        }

        // 2. Evaluate active daily quotas
        val quotas = PrefsManager.getAllDailyQuotas(this)
        if (quotas.isEmpty()) return

        val (startTime, endTime) = DataUsageManager.getTimeRange("today")
        val quotaBlocked = PrefsManager.getQuotaBlockedPackages(this)

        for ((pkg, quotaBytes) in quotas) {
            if (quotaBytes <= 0L || quotaBlocked.contains(pkg)) continue
            val uid = packageUidMap[pkg] ?: PrefsManager.getPackageUidMap(this)[pkg] ?: continue

            val stats = DataUsageManager.getUidStats(this, uid, startTime, endTime)
            val totalBytesToday = stats.first + stats.second + stats.third + stats.fourth

            if (totalBytesToday >= quotaBytes) {
                Log.d(TAG, "🚨 Daily Quota Reached for $pkg: $totalBytesToday >= $quotaBytes -> Enforcing TOTAL_BLACKOUT")
                PrefsManager.setAppQuotaBlocked(this, pkg, true)
                shieldedPackages.add(pkg)
                ShizukuManager.setAppIsolation(pkg, uid, AppIsolationMode.TOTAL_BLACKOUT, isForeground = false, blockNotifications = true)
                dispatchQuotaNotification(pkg, totalBytesToday, quotaBytes)
            }
        }
    }

    private fun dispatchQuotaNotification(packageName: String, usedBytes: Long, quotaBytes: Long) {
        val appName = getAppLabel(packageName)

        val extendIntent = Intent(this, AppShieldService::class.java).apply {
            action = ACTION_EXTEND_QUOTA
            putExtra(EXTRA_PACKAGE, packageName)
        }
        val extendPI = PendingIntent.getService(
            this,
            (packageName + "_extend").hashCode(),
            extendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AppShieldService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_PACKAGE, packageName)
        }
        val dismissPI = PendingIntent.getService(
            this,
            (packageName + "_dismiss_q").hashCode(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppPI = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, LEAK_CHANNEL_ID)
            .setContentTitle("🚨 Daily Data Limit Hit: $appName")
            .setContentText("Used ${DataUsageManager.formatBytes(usedBytes)} of ${DataUsageManager.formatBytes(quotaBytes)} limit. Auto-blocked until midnight.")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFFF5252.toInt())
            .setContentIntent(openAppPI)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "➕ Extend +500MB", extendPI)
            .addAction(0, "✕ Dismiss", dismissPI)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((packageName + "_quota").hashCode(), notification)
    }

    private val screenReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            handler.post {
                when (intent.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        if (PrefsManager.isScreenOffShield(this@AppShieldService)) {
                            Log.d(TAG, "Screen OFF detected: Enforcing strict lockdown on all shielded apps")
                            val blockNotifs = PrefsManager.isBlockNotifications(this@AppShieldService)
                            val savedBlackout = PrefsManager.getBlackoutPackages(this@AppShieldService)
                            val batch = mutableListOf<ShizukuManager.BatchAppRule>()
                            for (pkg in shieldedPackages) {
                                val uid = packageUidMap[pkg] ?: continue
                                val mode = if (savedBlackout.contains(pkg)) AppIsolationMode.TOTAL_BLACKOUT else AppIsolationMode.SMART_SHIELD
                                batch.add(ShizukuManager.BatchAppRule(pkg, uid, mode, isForeground = false))
                            }
                            if (batch.isNotEmpty()) {
                                ShizukuManager.applyBatchIsolationRules(batch, blockNotifications = blockNotifs)
                            }
                        }
                    }
                    Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> {
                        val currentForeground = getForegroundPackageName()
                        if (currentForeground.isNotEmpty()) {
                            val prevApp = lastForegroundPackage
                            lastForegroundPackage = currentForeground
                            onForegroundAppChanged(prevApp, currentForeground)
                        }
                    }
                }
            }
        }
    }

    private var lastObservedWifiState: Boolean? = null

    private val networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: android.net.Network, capabilities: android.net.NetworkCapabilities) {
            try {
                val isWifi = capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                // Filter out RSSI / link-speed jitter — only reapply if connection type actually flipped!
                if (lastObservedWifiState == isWifi) return
                lastObservedWifiState = isWifi
                if (::handler.isInitialized) {
                    handler.post { reapplyAllGranularRules() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "NetworkCallback capabilities error", e)
            }
        }

        override fun onLost(network: android.net.Network) {
            try {
                val isWifi = isWifiActive()
                if (lastObservedWifiState == isWifi) return
                lastObservedWifiState = isWifi
                if (::handler.isInitialized) {
                    handler.post { reapplyAllGranularRules() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "NetworkCallback onLost error", e)
            }
        }
    }

    private fun isWifiActive(): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            false
        }
    }

    override fun onCreate() {
        super.onCreate()
        workerThread = HandlerThread("AppShieldWorker").apply { start() }
        handler = Handler(workerThread.looper)
        startForegroundServiceNotification()
        loadPersistedState()
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            screenReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            cm?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (::workerThread.isInitialized) {
            handler.removeCallbacksAndMessages(null)
            workerThread.quitSafely()
        }
    }

    private fun loadPersistedState() {
        val savedPkgs = PrefsManager.getShieldedPackages(this)
        val savedUids = PrefsManager.getPackageUidMap(this)
        packageUidMap.clear()
        packageUidMap.putAll(savedUids)
        if (savedPkgs.isNotEmpty()) {
            shieldedPackages.clear()
            shieldedPackages.addAll(savedPkgs)
            Log.d(TAG, "Restored ${shieldedPackages.size} shielded packages from persistent storage")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always satisfy Android 12-15 startForegroundService() contract within 5s
        updateForegroundNotification()

        val action = intent?.action
        if (action == ACTION_QUICK_BLACKOUT) {
            val pkg = intent.getStringExtra(EXTRA_PACKAGE)
            if (pkg != null) {
                handler.post {
                    Log.d(TAG, "Quick Blackout triggered for $pkg")
                    PrefsManager.setAppIsolationMode(this@AppShieldService, pkg, AppIsolationMode.TOTAL_BLACKOUT)
                    shieldedPackages.add(pkg)
                    val uid = packageUidMap[pkg] ?: PrefsManager.getPackageUidMap(this@AppShieldService)[pkg]
                    if (uid != null) {
                        packageUidMap[pkg] = uid
                        ShizukuManager.setAppIsolation(pkg, uid, AppIsolationMode.TOTAL_BLACKOUT, isForeground = false, blockNotifications = true)
                    }
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel(pkg.hashCode())
                }
            }
            return START_STICKY
        }

        if (action == ACTION_DISMISS) {
            val pkg = intent.getStringExtra(EXTRA_PACKAGE)
            if (pkg != null) {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(pkg.hashCode())
                nm.cancel((pkg + "_quota").hashCode())
            }
            return START_STICKY
        }

        if (action == ACTION_EXTEND_QUOTA) {
            val pkg = intent.getStringExtra(EXTRA_PACKAGE)
            if (pkg != null) {
                handler.post {
                    Log.d(TAG, "Extend quota (+500MB) triggered for $pkg")
                    val currentQuota = PrefsManager.getAppDailyQuota(this@AppShieldService, pkg)
                    val newQuota = if (currentQuota > 0L) currentQuota + 500L * 1024L * 1024L else 500L * 1024L * 1024L
                    PrefsManager.setAppDailyQuota(this@AppShieldService, pkg, newQuota)
                    PrefsManager.setAppQuotaBlocked(this@AppShieldService, pkg, false)

                    val uid = packageUidMap[pkg] ?: PrefsManager.getPackageUidMap(this@AppShieldService)[pkg]
                    if (uid != null) {
                        val isBlackout = PrefsManager.getBlackoutPackages(this@AppShieldService).contains(pkg)
                        if (!isBlackout) {
                            val mode = PrefsManager.getAppIsolationMode(this@AppShieldService, pkg)
                            ShizukuManager.setAppIsolation(pkg, uid, mode, isForeground = false, blockNotifications = false)
                        }
                    }
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.cancel((pkg + "_quota").hashCode())
                }
            }
            return START_STICKY
        }

        if (action == ACTION_START || action == ACTION_UPDATE || action == null) {
            val packages = intent?.getStringArrayListExtra(EXTRA_PACKAGES)
            val uids = intent?.getIntegerArrayListExtra(EXTRA_UIDS)

            if (packages != null && uids != null) {
                shieldedPackages.clear()
                shieldedPackages.addAll(packages)
                packageUidMap.putAll(PrefsManager.getPackageUidMap(this))
                val minSize = minOf(packages.size, uids.size)
                for (i in 0 until minSize) {
                    packageUidMap[packages[i]] = uids[i]
                }
                PrefsManager.saveShieldedPackages(this, shieldedPackages)
                PrefsManager.savePackageUidMap(this, packageUidMap)
            } else {
                loadPersistedState()
            }

            // Immediately enforce granular restrictions in background worker thread
            handler.post {
                reapplyAllGranularRules()
            }

            if (!isRunning) {
                isRunning = true
                handler.post(checkForegroundRunnable)
                Log.d(TAG, "AppShieldService started monitoring ${shieldedPackages.size} apps.")
            }
        } else if (action == ACTION_STOP) {
            stopMonitoring()
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "NetCordon swiped from Recents — Foreground service continues running")
        // Service remains alive in background as ongoing foreground service
    }

    private fun reapplyAllGranularRules() {
        try {
            val currentForeground = getForegroundPackageName()
            val blockNotifs = PrefsManager.isBlockNotifications(this)
            val isWifi = isWifiActive()
            val savedSmart = PrefsManager.getSmartShieldPackages(this)
            val savedWifi = PrefsManager.getWifiBlockedPackages(this)
            val savedData = PrefsManager.getDataBlockedPackages(this)
            val savedBlackout = PrefsManager.getBlackoutPackages(this)
            val quotaBlocked = PrefsManager.getQuotaBlockedPackages(this)

            val allowedRules = mutableListOf<ShizukuManager.BatchAppRule>()
            val restrictedRules = mutableListOf<ShizukuManager.BatchAppRule>()

            for (pkg in shieldedPackages) {
                val uid = packageUidMap[pkg] ?: PrefsManager.getPackageUidMap(this)[pkg] ?: continue
                val isForeground = (pkg == currentForeground)
                val isBlackout = savedBlackout.contains(pkg) || quotaBlocked.contains(pkg)
                val isSmartShield = savedSmart.contains(pkg)
                val isWifiBlocked = savedWifi.contains(pkg)
                val isDataBlocked = savedData.contains(pkg)

                val mode = PrefsManager.computeEffectiveIsolationMode(
                    isBlackout = isBlackout,
                    isSmartShield = isSmartShield,
                    isWifiBlocked = isWifiBlocked,
                    isDataBlocked = isDataBlocked,
                    isWifiActive = isWifi,
                    isForeground = isForeground
                )

                val rule = ShizukuManager.BatchAppRule(pkg, uid, mode, isForeground)
                if (mode == AppIsolationMode.ALLOWED) {
                    allowedRules.add(rule)
                } else {
                    restrictedRules.add(rule)
                }
            }

            if (allowedRules.isNotEmpty()) {
                ShizukuManager.applyBatchIsolationRules(allowedRules, blockNotifications = false, enableChain3 = true)
            }
            if (restrictedRules.isNotEmpty()) {
                ShizukuManager.applyBatchIsolationRules(restrictedRules, blockNotifications = blockNotifs, enableChain3 = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in reapplyAllGranularRules", e)
        }
    }

    private fun onForegroundAppChanged(previousPackage: String, foregroundPackage: String) {
        val blockNotifs = PrefsManager.isBlockNotifications(this)
        val isWifi = isWifiActive()
        val savedSmart = PrefsManager.getSmartShieldPackages(this)
        val savedWifi = PrefsManager.getWifiBlockedPackages(this)
        val savedData = PrefsManager.getDataBlockedPackages(this)
        val savedBlackout = PrefsManager.getBlackoutPackages(this)
        val quotaBlocked = PrefsManager.getQuotaBlockedPackages(this)

        // Only update apps whose state actually changed
        val affected = mutableSetOf<String>()
        if (foregroundPackage.isNotEmpty() && shieldedPackages.contains(foregroundPackage)) {
            affected.add(foregroundPackage)
        }
        if (previousPackage.isNotEmpty() && shieldedPackages.contains(previousPackage)) {
            affected.add(previousPackage)
        }

        for (pkg in affected) {
            val uid = packageUidMap[pkg] ?: continue
            val isForeground = (pkg == foregroundPackage)
            val isBlackout = savedBlackout.contains(pkg) || quotaBlocked.contains(pkg)
            val isSmartShield = savedSmart.contains(pkg)
            val isWifiBlocked = savedWifi.contains(pkg)
            val isDataBlocked = savedData.contains(pkg)

            val mode = PrefsManager.computeEffectiveIsolationMode(
                isBlackout = isBlackout,
                isSmartShield = isSmartShield,
                isWifiBlocked = isWifiBlocked,
                isDataBlocked = isDataBlocked,
                isWifiActive = isWifi,
                isForeground = isForeground
            )

            ShizukuManager.setAppIsolation(
                packageName = pkg,
                uid = uid,
                mode = mode,
                isForeground = isForeground,
                blockNotifications = if (mode == AppIsolationMode.ALLOWED) false else blockNotifs
            )
            Log.d(TAG, "SmartShield Event: $pkg [Foreground: $isForeground] -> $mode")
        }
    }

    private fun getForegroundPackageName(): String {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            // Look back 15s on subsequent polls, or 5 minutes on cold start when lastForegroundPackage is empty
            val lookbackMs = if (lastForegroundPackage.isEmpty()) 300_000L else 15_000L
            val events = usm.queryEvents(time - lookbackMs, time)
            var lastEventPackage = ""
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                @Suppress("DEPRECATION")
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastEventPackage = event.packageName
                }
            }

            if (lastEventPackage.isNotEmpty()) {
                lastEventPackage
            } else if (lastForegroundPackage.isNotEmpty()) {
                // Keep current foreground package when user stays in the same activity for >15s
                lastForegroundPackage
            } else {
                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 60_000L, time)
                stats?.maxByOrNull { it.lastTimeUsed }?.packageName ?: ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting foreground app", e)
            lastForegroundPackage
        }
    }

    private fun stopMonitoring() {
        isRunning = false
        if (::handler.isInitialized) {
            handler.removeCallbacks(checkForegroundRunnable)
        }
        val allShielded = HashSet(
            shieldedPackages +
            PrefsManager.getShieldedPackages(this) +
            PrefsManager.getSmartShieldPackages(this) +
            PrefsManager.getBlackoutPackages(this) +
            PrefsManager.getWifiBlockedPackages(this) +
            PrefsManager.getDataBlockedPackages(this)
        )
        val uidMapSnapshot = HashMap(packageUidMap).apply {
            putAll(PrefsManager.getPackageUidMap(this@AppShieldService))
        }

        // Run rule restoration on a dedicated non-cancelled thread before service teardown
        Thread {
            try {
                val restoreRules = allShielded.mapNotNull { pkg ->
                    val uid = uidMapSnapshot[pkg] ?: return@mapNotNull null
                    ShizukuManager.BatchAppRule(pkg, uid, AppIsolationMode.ALLOWED, isForeground = true)
                }
                if (restoreRules.isNotEmpty()) {
                    ShizukuManager.applyBatchIsolationRules(restoreRules, blockNotifications = false, enableChain3 = false)
                }
                ShizukuManager.executeCommand("cmd connectivity set-chain3-enabled false")
            } catch (e: Exception) {
                Log.e(TAG, "Failed restoring rules on stopMonitoring", e)
            }
        }.start()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildForegroundNotification(): Notification {
        val channelId = "netcordon_shield_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isMin = PrefsManager.isMinNotif(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NetCordon Background Monitor",
                if (isMin) NotificationManager.IMPORTANCE_MIN else NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)

            val leakChannel = NotificationChannel(
                LEAK_CHANNEL_ID,
                "Background Data Leak Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when background apps attempt excessive network connections"
            }
            manager.createNotificationChannel(leakChannel)
        }

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(if (isMin) "NetCordon" else "NetCordon Active")
            .setContentText(if (isMin) "Protected" else "Background firewall and notification shield active")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFFF5252.toInt())
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(if (isMin) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startForegroundServiceNotification() {
        try {
            val notification = buildForegroundNotification()
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            androidx.core.app.ServiceCompat.startForeground(this, 101, notification, fgsType)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground notification", e)
        }
    }

    private fun updateForegroundNotification() {
        startForegroundServiceNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AppShieldService"
        const val ACTION_START  = "com.sachinmandawi.netcordon.START"
        const val ACTION_UPDATE = "com.sachinmandawi.netcordon.UPDATE"
        const val ACTION_UPDATE_RULES = "com.sachinmandawi.netcordon.UPDATE"
        const val ACTION_STOP   = "com.sachinmandawi.netcordon.STOP"
        const val ACTION_QUICK_BLACKOUT = "com.sachinmandawi.netcordon.QUICK_BLACKOUT"
        const val ACTION_DISMISS        = "com.sachinmandawi.netcordon.DISMISS_ALERT"
        const val ACTION_EXTEND_QUOTA   = "com.sachinmandawi.netcordon.EXTEND_QUOTA"
        const val EXTRA_PACKAGES = "extra_packages"
        const val EXTRA_UIDS     = "extra_uids"
        const val EXTRA_PACKAGE  = "extra_package"
        private const val LEAK_CHANNEL_ID = "netcordon_leak_channel"
    }
}
