package com.smartfirewall.shizuku

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class AppShieldService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var lastForegroundPackage = ""
    private val shieldedPackages = mutableSetOf<String>()
    private val packageUidMap = mutableMapOf<String, Int>()

    private val checkForegroundRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val currentApp = getForegroundPackageName()
            if (currentApp.isNotEmpty() && currentApp != lastForegroundPackage) {
                Log.d(TAG, "Foreground App Changed: $lastForegroundPackage -> $currentApp")
                lastForegroundPackage = currentApp
                onForegroundAppChanged(currentApp)
            }

            handler.postDelayed(this, 400) // Poll every 400ms
        }
    }

    private val screenReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    if (PrefsManager.isScreenOffShield(this@AppShieldService)) {
                        Log.d(TAG, "Screen OFF detected: Enforcing strict lockdown on all shielded apps")
                        val blockNotifs = PrefsManager.isBlockNotifications(this@AppShieldService)
                        val savedWifi = PrefsManager.getWifiBlockedPackages(this@AppShieldService)
                        val savedData = PrefsManager.getDataBlockedPackages(this@AppShieldService)
                        for (pkg in shieldedPackages) {
                            val uid = packageUidMap[pkg] ?: continue
                            val blockWifi = savedWifi.contains(pkg)
                            val blockData = savedData.contains(pkg)
                            ShizukuManager.setAppNetworkAccess(
                                packageName = pkg,
                                uid = uid,
                                blockWifi = blockWifi,
                                blockData = blockData,
                                blockNotifications = blockNotifs
                            )
                        }
                    }
                }
                Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> {
                    val currentForeground = getForegroundPackageName()
                    if (currentForeground.isNotEmpty()) {
                        onForegroundAppChanged(currentForeground)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        loadPersistedState()
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPersistedState() {
        val savedPkgs = PrefsManager.getShieldedPackages(this)
        val savedUids = PrefsManager.getPackageUidMap(this)
        if (savedPkgs.isNotEmpty()) {
            shieldedPackages.clear()
            shieldedPackages.addAll(savedPkgs)
            packageUidMap.clear()
            packageUidMap.putAll(savedUids)
            Log.d(TAG, "Restored ${shieldedPackages.size} shielded packages from persistent storage")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_START || action == ACTION_UPDATE) {
            val packages = intent?.getStringArrayListExtra(EXTRA_PACKAGES)
            val uids = intent?.getIntegerArrayListExtra(EXTRA_UIDS)

            if (packages != null && uids != null) {
                shieldedPackages.clear()
                shieldedPackages.addAll(packages)
                packageUidMap.clear()
                for (i in packages.indices) {
                    if (i < uids.size) {
                        packageUidMap[packages[i]] = uids[i]
                    }
                }
                // Save to persistent storage
                PrefsManager.saveShieldedPackages(this, shieldedPackages)
                PrefsManager.savePackageUidMap(this, packageUidMap)
            } else {
                loadPersistedState()
            }

            // Immediately enforce granular restrictions
            val currentForeground = getForegroundPackageName()
            val blockNotifs = PrefsManager.isBlockNotifications(this)
            val savedWifi = PrefsManager.getWifiBlockedPackages(this)
            val savedData = PrefsManager.getDataBlockedPackages(this)

            for ((pkg, uid) in packageUidMap) {
                val isForeground = (pkg == currentForeground)
                val blockWifi = !isForeground && savedWifi.contains(pkg)
                val blockData = !isForeground && savedData.contains(pkg)
                ShizukuManager.setAppNetworkAccess(
                    packageName = pkg,
                    uid = uid,
                    blockWifi = blockWifi,
                    blockData = blockData,
                    blockNotifications = if (isForeground) false else blockNotifs
                )
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

    private fun onForegroundAppChanged(foregroundPackage: String) {
        val blockNotifs = PrefsManager.isBlockNotifications(this)
        val savedWifi = PrefsManager.getWifiBlockedPackages(this)
        val savedData = PrefsManager.getDataBlockedPackages(this)

        for (pkg in shieldedPackages) {
            val uid = packageUidMap[pkg] ?: continue
            val isForeground = (pkg == foregroundPackage)
            val blockWifi = !isForeground && savedWifi.contains(pkg)
            val blockData = !isForeground && savedData.contains(pkg)

            ShizukuManager.setAppNetworkAccess(
                packageName = pkg,
                uid = uid,
                blockWifi = blockWifi,
                blockData = blockData,
                blockNotifications = if (isForeground) false else blockNotifs
            )
            Log.d(TAG, "App: $pkg (UID: $uid) -> ${if (isForeground) "ALLOWED (Foreground Active)" else "BLOCKED (WiFi: $blockWifi, Data: $blockData)"}")
        }
    }

    private fun getForegroundPackageName(): String {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val events = usm.queryEvents(time - 5000, time)
            var lastEventPackage = ""
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastEventPackage = event.packageName
                }
            }

            if (lastEventPackage.isNotEmpty()) lastEventPackage else lastForegroundPackage
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting foreground app", e)
            ""
        }
    }

    private fun stopMonitoring() {
        isRunning = false
        handler.removeCallbacks(checkForegroundRunnable)
        // Reset all policies when service stops
        for ((pkg, uid) in packageUidMap) {
            ShizukuManager.setAppNetworkAccess(
                packageName = pkg,
                uid = uid,
                blockWifi = false,
                blockData = false,
                blockNotifications = false
            )
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "netcordon_shield_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NetCordon Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NetCordon Active")
            .setContentText("Background firewall and notification shield active")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

        startForeground(101, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AppShieldService"
        const val ACTION_START  = "com.smartfirewall.shizuku.START"
        const val ACTION_UPDATE = "com.smartfirewall.shizuku.UPDATE"
        const val ACTION_STOP   = "com.smartfirewall.shizuku.STOP"
        const val EXTRA_PACKAGES = "extra_packages"
        const val EXTRA_UIDS     = "extra_uids"
    }
}
