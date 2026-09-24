package com.sachinmandawi.netcordon

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrefsManager {
    private const val PREFS_NAME = "smart_net_shield_prefs"

    private const val KEY_SETUP_COMPLETED       = "setup_completed"
    private const val KEY_WIFI_BLOCKED_PACKAGES = "wifi_blocked_packages"
    private const val KEY_DATA_BLOCKED_PACKAGES = "data_blocked_packages"
    private const val KEY_PACKAGE_UIDS          = "package_uids"
    private const val KEY_BLOCK_NOTIFS          = "block_notifications"
    private const val KEY_SCREEN_OFF_SHIELD     = "screen_off_shield"
    private const val KEY_SERVICE_ENABLED       = "service_enabled"
    private const val KEY_START_ON_BOOT         = "start_on_boot"
    private const val KEY_SHOW_PKG              = "show_package_names"
    private const val KEY_MIN_NOTIF             = "minimal_notification"
    private const val KEY_PRIVILEGE             = "privilege_provider"
    private const val KEY_APP_LOCK_ENABLED      = "app_lock_enabled"
    private const val KEY_LOCK_ON_SCREEN_OFF    = "lock_on_screen_off"
    private const val KEY_BLACKOUT_PACKAGES     = "blackout_packages"
    private const val KEY_BATTERY_IGNORED       = "battery_optimization_ignored"
    private const val KEY_ONBOARDING_COMPLETED  = "onboarding_completed"
    private const val KEY_SCHEDULES             = "firewall_schedules"
    private const val KEY_LEAK_ALERTS_ENABLED   = "leak_alerts_enabled"
    private const val KEY_LEAK_ALERT_THRESHOLD  = "leak_alert_threshold"
    private const val KEY_BLOCKED_ATTEMPTS      = "blocked_attempts_today"
    private const val KEY_BLOCKED_DATE          = "blocked_attempts_date"
    private const val KEY_DARK_MODE             = "dark_mode_enabled"
    private const val KEY_DAILY_QUOTAS          = "app_daily_quotas"
    private const val KEY_QUOTA_BLOCKED_PACKAGES = "quota_blocked_packages"
    private const val KEY_QUOTA_BLOCKED_DATE    = "quota_blocked_date"
    private const val KEY_FLOATING_SPEEDOMETER  = "floating_speedometer_enabled"
    private const val KEY_SMART_SHIELD_PACKAGES = "smart_shield_packages"
    private const val KEY_SHIELDED_PACKAGES     = "shielded_packages"
    private const val KEY_BASE_FIREWALL_MODES   = "base_firewall_modes"
    private const val KEY_AD_FREE_UNTIL         = "ad_free_until_timestamp"
    private const val KEY_LAST_INTERSTITIAL     = "last_interstitial_shown_timestamp"
    private const val KEY_LAST_NAV_INTERSTITIAL = "last_nav_interstitial_ts"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /* ── Onboarding Walkthrough Gate ── */
    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    /* ── Setup Completed Gate ── */
    fun isSetupCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SETUP_COMPLETED, false)
    }

    fun setSetupCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }

    /* ── WiFi Blocked Packages ── */
    fun getWifiBlockedPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_WIFI_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveWifiBlockedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_WIFI_BLOCKED_PACKAGES, HashSet(packages)).apply()
    }

    fun setAppWifiBlocked(context: Context, packageName: String, blocked: Boolean) {
        val set = getWifiBlockedPackages(context).toMutableSet()
        if (blocked) set.add(packageName) else set.remove(packageName)
        saveWifiBlockedPackages(context, set)
    }

    fun isAppWifiBlocked(context: Context, packageName: String): Boolean {
        return getWifiBlockedPackages(context).contains(packageName)
    }

    /* ── Data Blocked Packages ── */
    fun getDataBlockedPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DATA_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveDataBlockedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_DATA_BLOCKED_PACKAGES, HashSet(packages)).apply()
    }

    fun setAppDataBlocked(context: Context, packageName: String, blocked: Boolean) {
        val set = getDataBlockedPackages(context).toMutableSet()
        if (blocked) set.add(packageName) else set.remove(packageName)
        saveDataBlockedPackages(context, set)
    }

    fun isAppDataBlocked(context: Context, packageName: String): Boolean {
        return getDataBlockedPackages(context).contains(packageName)
    }

    /* ── Smart Shield Packages ── */
    fun getSmartShieldPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_SMART_SHIELD_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveSmartShieldPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_SMART_SHIELD_PACKAGES, HashSet(packages)).apply()
    }

    fun setAppSmartShield(context: Context, packageName: String, enabled: Boolean) {
        val set = getSmartShieldPackages(context).toMutableSet()
        if (enabled) set.add(packageName) else set.remove(packageName)
        saveSmartShieldPackages(context, set)
    }

    fun isAppSmartShield(context: Context, packageName: String): Boolean {
        return getSmartShieldPackages(context).contains(packageName)
    }

    /* ── All Shielded Packages (Combined Smart + WiFi + Data + Blackout) ── */
    fun getShieldedPackages(context: Context): Set<String> {
        val smart = getSmartShieldPackages(context)
        val wifi = getWifiBlockedPackages(context)
        val data = getDataBlockedPackages(context)
        val blackout = getBlackoutPackages(context)
        val direct = getPrefs(context).getStringSet(KEY_SHIELDED_PACKAGES, emptySet()) ?: emptySet()
        return smart + wifi + data + blackout + direct
    }

    fun saveShieldedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_SHIELDED_PACKAGES, HashSet(packages)).apply()
    }

    /* ── Package UIDs Map ── */
    fun getPackageUidMap(context: Context): Map<String, Int> {
        val raw = getPrefs(context).getString(KEY_PACKAGE_UIDS, "") ?: ""
        if (raw.isEmpty()) return emptyMap()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: return@mapNotNull null) else null
        }.toMap()
    }

    fun savePackageUidMap(context: Context, map: Map<String, Int>) {
        val raw = map.entries.joinToString(";") { "${it.key}:${it.value}" }
        getPrefs(context).edit().putString(KEY_PACKAGE_UIDS, raw).apply()
    }

    /* ── Block Notifications for Restricted Apps ── */
    fun isBlockNotifications(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BLOCK_NOTIFS, true)
    }

    fun setBlockNotifications(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCK_NOTIFS, enabled).apply()
    }

    /* ── Screen Off Shield Rule ── */
    fun isScreenOffShield(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCREEN_OFF_SHIELD, false)
    }

    fun setScreenOffShield(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SCREEN_OFF_SHIELD, enabled).apply()
    }

    /* ── Service State & Settings ── */
    fun isServiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICE_ENABLED, true)
    }

    fun setServiceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isStartOnBoot(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_START_ON_BOOT, true)
    }

    fun setStartOnBoot(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_START_ON_BOOT, enabled).apply()
    }

    fun isShowPkg(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_PKG, true)
    }

    fun setShowPkg(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_PKG, enabled).apply()
    }

    fun isMinNotif(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_MIN_NOTIF, false)
    }

    fun setMinNotif(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_MIN_NOTIF, enabled).apply()
    }

    fun getPrivilege(context: Context): String {
        return getPrefs(context).getString(KEY_PRIVILEGE, "shizuku") ?: "shizuku"
    }

    fun setPrivilege(context: Context, privilege: String) {
        getPrefs(context).edit().putString(KEY_PRIVILEGE, privilege).apply()
    }

    /* ── Theme Mode (Dark / Light) ── */
    fun isDarkMode(context: Context): Boolean {
        // Default to false (Light Mode) as requested
        return getPrefs(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    /* ── Biometric / PIN App Lock ── */
    fun isAppLockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun isLockOnScreenOff(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOCK_ON_SCREEN_OFF, true)
    }

    fun setLockOnScreenOff(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOCK_ON_SCREEN_OFF, enabled).apply()
    }

    /* ── Total Blackout Packages (Always Cut / Zero Foreground Internet) ── */
    fun getBlackoutPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_BLACKOUT_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveBlackoutPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_BLACKOUT_PACKAGES, HashSet(packages)).apply()
    }

    fun computeEffectiveIsolationMode(
        isBlackout: Boolean,
        isSmartShield: Boolean,
        isWifiBlocked: Boolean,
        isDataBlocked: Boolean,
        isWifiActive: Boolean,
        isForeground: Boolean
    ): AppIsolationMode {
        if (isBlackout) return AppIsolationMode.TOTAL_BLACKOUT
        if (isWifiBlocked && isDataBlocked) return AppIsolationMode.TOTAL_BLACKOUT

        val isBlockedOnCurrentNet = (isWifiActive && isWifiBlocked) || (!isWifiActive && isDataBlocked)
        if (isBlockedOnCurrentNet) return AppIsolationMode.TOTAL_BLACKOUT

        return if (isSmartShield) {
            if (isForeground) AppIsolationMode.ALLOWED else AppIsolationMode.SMART_SHIELD
        } else {
            AppIsolationMode.ALLOWED
        }
    }

    fun getAppIsolationMode(context: Context, packageName: String): AppIsolationMode {
        val blackout = getBlackoutPackages(context)
        if (blackout.contains(packageName)) return AppIsolationMode.TOTAL_BLACKOUT
        val smart = getSmartShieldPackages(context)
        if (smart.contains(packageName)) return AppIsolationMode.SMART_SHIELD
        return AppIsolationMode.ALLOWED
    }

    fun getBaseAppIsolationMode(context: Context, packageName: String): AppIsolationMode {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_BASE_FIREWALL_MODES, null)
        if (!json.isNullOrEmpty()) {
            try {
                val obj = JSONObject(json)
                if (obj.has(packageName)) {
                    return AppIsolationMode.valueOf(obj.getString(packageName))
                }
            } catch (e: Exception) {
                // fallback to current sets
            }
        }
        return getAppIsolationMode(context, packageName)
    }

    fun saveBaseAppIsolationMode(context: Context, packageName: String, mode: AppIsolationMode) {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_BASE_FIREWALL_MODES, null)
        val obj = try {
            if (json.isNullOrEmpty()) JSONObject() else JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
        obj.put(packageName, mode.name)
        prefs.edit().putString(KEY_BASE_FIREWALL_MODES, obj.toString()).apply()
    }

    /**
     * User-configured permanent firewall rule change from the Firewall UI.
     * Records base mode and applies rule.
     */
    fun setAppIsolationMode(context: Context, packageName: String, mode: AppIsolationMode) {
        saveBaseAppIsolationMode(context, packageName, mode)
        applyScheduledIsolationMode(context, packageName, mode)
    }

    /**
     * Temporary scheduled rule application (does NOT overwrite base firewall rule).
     */
    fun applyScheduledIsolationMode(context: Context, packageName: String, mode: AppIsolationMode) {
        val smart = getSmartShieldPackages(context).toMutableSet()
        val wifi = getWifiBlockedPackages(context).toMutableSet()
        val data = getDataBlockedPackages(context).toMutableSet()
        val blackout = getBlackoutPackages(context).toMutableSet()

        when (mode) {
            AppIsolationMode.ALLOWED -> {
                smart.remove(packageName)
                blackout.remove(packageName)
            }
            AppIsolationMode.SMART_SHIELD -> {
                smart.add(packageName)
                // Do NOT touch wifi/data sets — they are independent per-network controls
                // that coexist with Smart Shield (e.g. Smart Shield + Wi-Fi blocked)
                blackout.remove(packageName)
            }
            AppIsolationMode.TOTAL_BLACKOUT -> {
                smart.remove(packageName)
                blackout.add(packageName)
            }
        }

        saveSmartShieldPackages(context, smart)
        saveWifiBlockedPackages(context, wifi)
        saveDataBlockedPackages(context, data)
        saveBlackoutPackages(context, blackout)
    }

    /**
     * When a schedule ends or is cancelled, restores all packages in [packages]
     * to another currently active schedule's mode or their permanent base firewall configuration.
     */
    fun restoreBaseFirewallRules(context: Context, packages: Set<String>, excludingScheduleId: String? = null) {
        val activeSchedules = getSchedules(context).filter {
            it.isEnabled && it.id != excludingScheduleId && it.isCurrentlyActive()
        }
        for (pkg in packages) {
            val overriding = activeSchedules.firstOrNull { it.targetPackages.contains(pkg) }
            if (overriding != null) {
                applyScheduledIsolationMode(context, pkg, overriding.mode)
            } else {
                val baseMode = getBaseAppIsolationMode(context, pkg)
                applyScheduledIsolationMode(context, pkg, baseMode)
            }
        }
    }

    /* ── Battery Optimization Whitelist ── */
    fun isBatteryIgnored(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BATTERY_IGNORED, false)
    }

    fun setBatteryIgnored(context: Context, ignored: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BATTERY_IGNORED, ignored).apply()
    }

    /* ── Scheduled Firewall Engine ── */
    fun getSchedules(context: Context): List<FirewallSchedule> {
        val json = getPrefs(context).getString(KEY_SCHEDULES, null)
        if (json.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val array = JSONArray(json)
            val list = mutableListOf<FirewallSchedule>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val daysArray = obj.getJSONArray("days")
                val days = mutableSetOf<Int>()
                for (d in 0 until daysArray.length()) days.add(daysArray.getInt(d))

                val pkgsArray = obj.getJSONArray("pkgs")
                val pkgs = mutableSetOf<String>()
                for (p in 0 until pkgsArray.length()) pkgs.add(pkgsArray.getString(p))

                val modeStr = obj.optString("mode", AppIsolationMode.TOTAL_BLACKOUT.name)
                val mode = try { AppIsolationMode.valueOf(modeStr) } catch (e: Exception) { AppIsolationMode.TOTAL_BLACKOUT }

                list.add(
                    FirewallSchedule(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        startHour = obj.getInt("startH"),
                        startMinute = obj.getInt("startM"),
                        endHour = obj.getInt("endH"),
                        endMinute = obj.getInt("endM"),
                        daysOfWeek = days,
                        targetPackages = pkgs,
                        mode = mode,
                        isEnabled = obj.getBoolean("enabled")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveSchedules(context: Context, schedules: List<FirewallSchedule>) {
        try {
            val array = JSONArray()
            for (s in schedules) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("startH", s.startHour)
                    put("startM", s.startMinute)
                    put("endH", s.endHour)
                    put("endM", s.endMinute)
                    put("enabled", s.isEnabled)
                    put("mode", s.mode.name)

                    val daysArray = JSONArray()
                    s.daysOfWeek.forEach { daysArray.put(it) }
                    put("days", daysArray)

                    val pkgsArray = JSONArray()
                    s.targetPackages.forEach { pkgsArray.put(it) }
                    put("pkgs", pkgsArray)
                }
                array.put(obj)
            }
            getPrefs(context).edit().putString(KEY_SCHEDULES, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* ── Leak Alerts & Tracker Shield ── */
    fun isLeakAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LEAK_ALERTS_ENABLED, true)
    }

    fun setLeakAlertsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LEAK_ALERTS_ENABLED, enabled).apply()
    }

    fun getLeakAlertThreshold(context: Context): Int {
        return getPrefs(context).getInt(KEY_LEAK_ALERT_THRESHOLD, 25)
    }

    fun setLeakAlertThreshold(context: Context, threshold: Int) {
        getPrefs(context).edit().putInt(KEY_LEAK_ALERT_THRESHOLD, threshold).apply()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun recordBlockedAttempt(context: Context, packageName: String, count: Int = 1) {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_BLOCKED_DATE, "")

        val attempts = if (savedDate == today) {
            getTodayBlockedAttempts(context).toMutableMap()
        } else {
            mutableMapOf()
        }

        attempts[packageName] = (attempts[packageName] ?: 0) + count

        val json = JSONObject(attempts as Map<*, *>).toString()
        prefs.edit()
            .putString(KEY_BLOCKED_DATE, today)
            .putString(KEY_BLOCKED_ATTEMPTS, json)
            .apply()
    }

    fun getTodayBlockedAttempts(context: Context): Map<String, Int> {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_BLOCKED_DATE, "")
        if (savedDate != today) return emptyMap()

        val json = prefs.getString(KEY_BLOCKED_ATTEMPTS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getInt(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getTodayTotalBlockedAttempts(context: Context): Int {
        return getTodayBlockedAttempts(context).values.sum()
    }

    /* ── Daily Data Quota / Budget Limit per App ── */
    fun getAppDailyQuota(context: Context, packageName: String): Long {
        val json = getPrefs(context).getString(KEY_DAILY_QUOTAS, null) ?: return 0L
        return try {
            val obj = JSONObject(json)
            obj.optLong(packageName, 0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun setAppDailyQuota(context: Context, packageName: String, quotaBytes: Long) {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_DAILY_QUOTAS, null)
        val obj = try {
            if (json.isNullOrEmpty()) JSONObject() else JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }

        if (quotaBytes <= 0L) {
            obj.remove(packageName)
        } else {
            obj.put(packageName, quotaBytes)
        }
        prefs.edit().putString(KEY_DAILY_QUOTAS, obj.toString()).apply()
    }

    fun getAllDailyQuotas(context: Context): Map<String, Long> {
        val json = getPrefs(context).getString(KEY_DAILY_QUOTAS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Long>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.getLong(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getQuotaBlockedPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_QUOTA_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun setAppQuotaBlocked(context: Context, packageName: String, blocked: Boolean) {
        val set = getQuotaBlockedPackages(context).toMutableSet()
        if (blocked) set.add(packageName) else set.remove(packageName)
        getPrefs(context).edit().putStringSet(KEY_QUOTA_BLOCKED_PACKAGES, set).apply()
    }

    fun checkAndResetQuotaDaily(context: Context): Set<String> {
        val prefs = getPrefs(context)
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_QUOTA_BLOCKED_DATE, "")
        if (savedDate != today) {
            val previouslyBlocked = getQuotaBlockedPackages(context)
            prefs.edit()
                .putString(KEY_QUOTA_BLOCKED_DATE, today)
                .putStringSet(KEY_QUOTA_BLOCKED_PACKAGES, emptySet())
                .apply()
            return previouslyBlocked
        }
        return emptySet()
    }

    /* ── Floating Speedometer & Live Leaker Widget ── */
    fun isFloatingSpeedometerEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FLOATING_SPEEDOMETER, false)
    }

    fun setFloatingSpeedometerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_FLOATING_SPEEDOMETER, enabled).apply()
    }

    /* ── Backup & Restore Firewall Profiles (Export / Import JSON) ── */
    fun exportProfileToJson(context: Context): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("appName", "NetCordon")

        // Blackout & Smart Shield apps
        val blackoutArray = JSONArray()
        getBlackoutPackages(context).forEach { blackoutArray.put(it) }
        root.put("blackoutPackages", blackoutArray)

        val smartShieldArray = JSONArray()
        getSmartShieldPackages(context).forEach { smartShieldArray.put(it) }
        root.put("smartShieldPackages", smartShieldArray)

        val wifiArray = JSONArray()
        getWifiBlockedPackages(context).forEach { wifiArray.put(it) }
        root.put("wifiBlockedPackages", wifiArray)

        val dataArray = JSONArray()
        getDataBlockedPackages(context).forEach { dataArray.put(it) }
        root.put("dataBlockedPackages", dataArray)

        // Schedules
        val schedulesArray = JSONArray()
        for (s in getSchedules(context)) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("startH", s.startHour)
                put("startM", s.startMinute)
                put("endH", s.endHour)
                put("endM", s.endMinute)
                val daysArr = JSONArray()
                s.daysOfWeek.forEach { daysArr.put(it) }
                put("days", daysArr)
                val pkgsArr = JSONArray()
                s.targetPackages.forEach { pkgsArr.put(it) }
                put("pkgs", pkgsArr)
                put("mode", s.mode.name)
                put("enabled", s.isEnabled)
            }
            schedulesArray.put(sObj)
        }
        root.put("schedules", schedulesArray)

        // Quotas
        val quotasObj = JSONObject()
        getAllDailyQuotas(context).forEach { (pkg, bytes) ->
            quotasObj.put(pkg, bytes)
        }
        root.put("dailyQuotas", quotasObj)

        // Settings
        val settingsObj = JSONObject().apply {
            put("screenOffShield", isScreenOffShield(context))
            put("blockNotifications", isBlockNotifications(context))
            put("leakAlertsEnabled", isLeakAlertsEnabled(context))
            put("minNotif", isMinNotif(context))
            put("startOnBoot", isStartOnBoot(context))
            put("showPkg", isShowPkg(context))
            put("floatingSpeedometer", isFloatingSpeedometerEnabled(context))
        }
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    fun parseProfileSummary(jsonStr: String): ProfileImportSummary? {
        return try {
            val root = JSONObject(jsonStr)
            val blackoutCount = root.optJSONArray("blackoutPackages")?.length() ?: 0
            val smartShieldCount = root.optJSONArray("smartShieldPackages")?.length() ?: 0
            val schedulesCount = root.optJSONArray("schedules")?.length() ?: 0
            val quotasCount = root.optJSONObject("dailyQuotas")?.length() ?: 0
            ProfileImportSummary(blackoutCount, smartShieldCount, schedulesCount, quotasCount)
        } catch (e: Exception) {
            null
        }
    }

    fun importProfileFromJson(context: Context, jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)

            // Blackout packages
            val blackoutArray = root.optJSONArray("blackoutPackages")
            val blackoutSet = mutableSetOf<String>()
            if (blackoutArray != null) {
                for (i in 0 until blackoutArray.length()) blackoutSet.add(blackoutArray.getString(i))
            }
            saveBlackoutPackages(context, blackoutSet)

            // Smart shield packages
            val smartShieldArray = root.optJSONArray("smartShieldPackages")
            val smartShieldSet = mutableSetOf<String>()
            if (smartShieldArray != null) {
                for (i in 0 until smartShieldArray.length()) smartShieldSet.add(smartShieldArray.getString(i))
            }
            saveSmartShieldPackages(context, smartShieldSet)

            // Wi-Fi blocked packages
            val wifiArray = root.optJSONArray("wifiBlockedPackages")
            if (wifiArray != null) {
                val wifiSet = mutableSetOf<String>()
                for (i in 0 until wifiArray.length()) wifiSet.add(wifiArray.getString(i))
                saveWifiBlockedPackages(context, wifiSet)
            }

            // Mobile Data blocked packages
            val dataArray = root.optJSONArray("dataBlockedPackages")
            if (dataArray != null) {
                val dataSet = mutableSetOf<String>()
                for (i in 0 until dataArray.length()) dataSet.add(dataArray.getString(i))
                saveDataBlockedPackages(context, dataSet)
            }

            // Schedules
            val schedulesArray = root.optJSONArray("schedules")
            if (schedulesArray != null) {
                val list = mutableListOf<FirewallSchedule>()
                for (i in 0 until schedulesArray.length()) {
                    val sObj = schedulesArray.getJSONObject(i)
                    val daysArr = sObj.getJSONArray("days")
                    val days = mutableSetOf<Int>()
                    for (d in 0 until daysArr.length()) days.add(daysArr.getInt(d))
                    val pkgsArr = sObj.getJSONArray("pkgs")
                    val pkgs = mutableSetOf<String>()
                    for (p in 0 until pkgsArr.length()) pkgs.add(pkgsArr.getString(p))
                    val modeStr = sObj.optString("mode", AppIsolationMode.TOTAL_BLACKOUT.name)
                    val mode = try { AppIsolationMode.valueOf(modeStr) } catch (e: Exception) { AppIsolationMode.TOTAL_BLACKOUT }

                    list.add(
                        FirewallSchedule(
                            id = sObj.getString("id"),
                            title = sObj.getString("title"),
                            startHour = sObj.getInt("startH"),
                            startMinute = sObj.getInt("startM"),
                            endHour = sObj.getInt("endH"),
                            endMinute = sObj.getInt("endM"),
                            daysOfWeek = days,
                            targetPackages = pkgs,
                            mode = mode,
                            isEnabled = sObj.getBoolean("enabled")
                        )
                    )
                }
                saveSchedules(context, list)
            }

            // Quotas
            val quotasObj = root.optJSONObject("dailyQuotas")
            if (quotasObj != null) {
                getPrefs(context).edit().putString(KEY_DAILY_QUOTAS, quotasObj.toString()).apply()
            }

            // Settings
            val settingsObj = root.optJSONObject("settings")
            if (settingsObj != null) {
                if (settingsObj.has("screenOffShield")) setScreenOffShield(context, settingsObj.getBoolean("screenOffShield"))
                if (settingsObj.has("blockNotifications")) setBlockNotifications(context, settingsObj.getBoolean("blockNotifications"))
                if (settingsObj.has("leakAlertsEnabled")) setLeakAlertsEnabled(context, settingsObj.getBoolean("leakAlertsEnabled"))
                if (settingsObj.has("minNotif")) setMinNotif(context, settingsObj.getBoolean("minNotif"))
                if (settingsObj.has("startOnBoot")) setStartOnBoot(context, settingsObj.getBoolean("startOnBoot"))
                if (settingsObj.has("showPkg")) setShowPkg(context, settingsObj.getBoolean("showPkg"))
                if (settingsObj.has("floatingSpeedometer")) setFloatingSpeedometerEnabled(context, settingsObj.getBoolean("floatingSpeedometer"))
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /* ── 24h VIP Ad-Free Pass & Monetization ── */
    fun getAdFreeUntil(context: Context): Long {
        return getPrefs(context).getLong(KEY_AD_FREE_UNTIL, 0L)
    }

    fun isAdFreeActive(context: Context): Boolean {
        return System.currentTimeMillis() < getAdFreeUntil(context)
    }

    fun getAdFreeRemainingMillis(context: Context): Long {
        val remaining = getAdFreeUntil(context) - System.currentTimeMillis()
        return if (remaining > 0L) remaining else 0L
    }

    fun grantAdFreeHours(context: Context, hours: Long = 12): Long {
        val now = System.currentTimeMillis()
        val currentExpiry = getAdFreeUntil(context)
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val additionalMillis = hours * 60 * 60 * 1000L
        // Cap max stacked time to 72 hours from now
        val maxAllowedExpiry = now + (72 * 60 * 60 * 1000L)
        val newExpiry = (baseTime + additionalMillis).coerceAtMost(maxAllowedExpiry)
        getPrefs(context).edit().putLong(KEY_AD_FREE_UNTIL, newExpiry).apply()
        return newExpiry
    }

    fun grantAdFreeDoubleBoost(context: Context): Long {
        val now = System.currentTimeMillis()
        val targetExpiry = now + (48 * 60 * 60 * 1000L) // 2 Full Days (48h)
        val currentExpiry = getAdFreeUntil(context)
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val boostedExpiry = (baseTime + (36 * 60 * 60 * 1000L)).coerceAtLeast(targetExpiry)
        val maxAllowedExpiry = now + (72 * 60 * 60 * 1000L)
        val newExpiry = boostedExpiry.coerceAtMost(maxAllowedExpiry)
        getPrefs(context).edit().putLong(KEY_AD_FREE_UNTIL, newExpiry).apply()
        return newExpiry
    }

    fun resetAdFreePass(context: Context) {
        getPrefs(context).edit().remove(KEY_AD_FREE_UNTIL).apply()
    }

    fun shouldShowInterstitial(context: Context, minIntervalSeconds: Long = 20): Boolean {
        if (isAdFreeActive(context)) return false
        val lastShown = getPrefs(context).getLong(KEY_LAST_INTERSTITIAL, 0L)
        val elapsed = System.currentTimeMillis() - lastShown
        return elapsed >= (minIntervalSeconds * 1000L)
    }

    fun recordInterstitialShown(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_INTERSTITIAL, System.currentTimeMillis()).apply()
    }

    fun shouldShowNavInterstitial(context: Context, intervalSeconds: Long = 300): Boolean {
        if (isAdFreeActive(context)) return false
        val lastShown = getPrefs(context).getLong(KEY_LAST_NAV_INTERSTITIAL, 0L)
        val elapsed = System.currentTimeMillis() - lastShown
        return elapsed >= (intervalSeconds * 1000L)
    }

    fun recordNavInterstitialShown(context: Context) {
        getPrefs(context).edit().putLong(KEY_LAST_NAV_INTERSTITIAL, System.currentTimeMillis()).apply()
    }
}

data class ProfileImportSummary(
    val blackoutCount: Int,
    val smartShieldCount: Int,
    val schedulesCount: Int,
    val quotasCount: Int
)

data class FirewallSchedule(
    val id: String,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int>, // 1 = Sun, 2 = Mon ... 7 = Sat
    val targetPackages: Set<String>,
    val mode: AppIsolationMode,
    val isEnabled: Boolean
) {
    fun isCurrentlyActive(now: java.util.Calendar = java.util.Calendar.getInstance()): Boolean {
        if (!isEnabled || daysOfWeek.isEmpty()) return false

        val currentDay = now.get(java.util.Calendar.DAY_OF_WEEK)
        val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        if (startMinutes == endMinutes) {
            return daysOfWeek.contains(currentDay)
        }

        return if (startMinutes < endMinutes) {
            daysOfWeek.contains(currentDay) && currentMinutes in startMinutes until endMinutes
        } else {
            if (currentMinutes >= startMinutes) {
                daysOfWeek.contains(currentDay)
            } else if (currentMinutes < endMinutes) {
                val prevCal = (now.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
                val prevDay = prevCal.get(java.util.Calendar.DAY_OF_WEEK)
                daysOfWeek.contains(prevDay)
            } else {
                false
            }
        }
    }
}

enum class AppIsolationMode {
    ALLOWED,
    SMART_SHIELD,
    TOTAL_BLACKOUT
}
