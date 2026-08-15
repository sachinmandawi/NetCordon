package com.smartfirewall.shizuku

import android.content.Context
import android.content.SharedPreferences

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

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    /* ── Data Blocked Packages ── */
    fun getDataBlockedPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DATA_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun saveDataBlockedPackages(context: Context, packages: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_DATA_BLOCKED_PACKAGES, HashSet(packages)).apply()
    }

    /* ── All Shielded Packages (Combined WiFi + Data) ── */
    fun getShieldedPackages(context: Context): Set<String> {
        val wifi = getWifiBlockedPackages(context)
        val data = getDataBlockedPackages(context)
        return wifi + data
    }

    fun saveShieldedPackages(context: Context, packages: Set<String>) {
        saveWifiBlockedPackages(context, packages)
        saveDataBlockedPackages(context, packages)
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
}
