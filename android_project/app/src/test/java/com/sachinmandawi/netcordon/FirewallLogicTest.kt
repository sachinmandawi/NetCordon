package com.sachinmandawi.netcordon

import org.junit.Assert.assertEquals
import org.junit.Test

class FirewallLogicTest {

    // Simulates the exact calculation logic used in AppShieldService and MainActivity
    // Priority: isBlackout/quota > dual-blocked > current net blocked > Smart Shield FG/BG > ALLOWED
    private fun computeEffectiveMode(
        isWifiActive: Boolean,
        wifiBlocked: Boolean,
        dataBlocked: Boolean,
        isBlackout: Boolean,
        quotaExceeded: Boolean,
        isForeground: Boolean,
        baseMode: AppIsolationMode
    ): AppIsolationMode {
        if (isBlackout || quotaExceeded) return AppIsolationMode.TOTAL_BLACKOUT
        if (wifiBlocked && dataBlocked) return AppIsolationMode.TOTAL_BLACKOUT

        val isBlockedOnCurrentNet = (isWifiActive && wifiBlocked) || (!isWifiActive && dataBlocked)
        if (isBlockedOnCurrentNet) return AppIsolationMode.TOTAL_BLACKOUT

        return if (baseMode == AppIsolationMode.SMART_SHIELD) {
            if (isForeground) AppIsolationMode.ALLOWED else AppIsolationMode.SMART_SHIELD
        } else {
            AppIsolationMode.ALLOWED
        }
    }

    @Test
    fun testWifiBlockedOnWifiNetwork() {
        // App has Wi-Fi blocked, but Mobile Data allowed. Phone is on Wi-Fi.
        val mode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        // MUST BE TOTAL_BLACKOUT even when foreground!
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, mode)
    }

    @Test
    fun testWifiBlockedOnMobileNetwork() {
        // App has Wi-Fi blocked, but Mobile Data allowed. Phone is on Mobile 5G.
        val mode = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        // MUST BE ALLOWED because phone is on Mobile Data!
        assertEquals(AppIsolationMode.ALLOWED, mode)
    }

    @Test
    fun testMobileDataBlockedOnMobileNetwork() {
        // App has Mobile Data blocked, but Wi-Fi allowed. Phone is on 4G/5G.
        val mode = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        // MUST BE TOTAL_BLACKOUT on Mobile Data!
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, mode)
    }

    @Test
    fun testSmartShieldForegroundAndBackground() {
        val fgMode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, fgMode)

        val bgMode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.SMART_SHIELD, bgMode)
    }

    @Test
    fun testMobileDataBlockedOnWifiNetwork() {
        // App has Mobile Data blocked, but Wi-Fi allowed. Phone is on Wi-Fi.
        val mode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        // MUST BE ALLOWED because phone is on Wi-Fi!
        assertEquals(AppIsolationMode.ALLOWED, mode)
    }

    @Test
    fun testTotalBlackoutForegroundAndBackground() {
        val fgMode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = true,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.TOTAL_BLACKOUT
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, fgMode)

        val bgMode = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = true,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.TOTAL_BLACKOUT
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, bgMode)
    }

    @Test
    fun testBothWifiAndDataBlockedForcesBlackout() {
        // If user separately blocks BOTH Wi-Fi and Data (not Smart Shield), it's Total Blackout
        val mode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = true,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, mode)
    }

    @Test
    fun testQuotaExceededForcesBlackout() {
        val fgMode = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = true,
            isForeground = true,
            baseMode = AppIsolationMode.ALLOWED
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, fgMode)
    }

    @Test
    fun testSmartShieldAlwaysAllowedInForeground() {
        // Smart Shield app: when opened, always gets full internet regardless of any stale flags
        val onWifiFg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onWifiFg)

        val onMobileFg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onMobileFg)
    }

    @Test
    fun testSmartShieldAlwaysCutInBackground() {
        // Smart Shield app: when closed/minimized, cuts internet on both interfaces
        val onWifiBg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.SMART_SHIELD, onWifiBg)

        val onMobileBg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.SMART_SHIELD, onMobileBg)
    }

    @Test
    fun testSmartShieldWithSelectiveWifiBlock() {
        // App is on Smart Shield, but user also blocked Wi-Fi
        // On Wi-Fi: Even in foreground, must be TOTAL_BLACKOUT (0 KB/s)!
        val onWifiFg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, onWifiFg)

        // On Mobile Data: In foreground, ALLOWED! In background, SMART_SHIELD!
        val onMobileFg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onMobileFg)

        val onMobileBg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.SMART_SHIELD, onMobileBg)
    }

    @Test
    fun testSmartShieldWithSelectiveDataBlock() {
        // App is on Smart Shield, but user also blocked Mobile Data
        // On Mobile Data: Even in foreground, must be TOTAL_BLACKOUT (0 KB/s)!
        val onMobileFg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, onMobileFg)

        // On Wi-Fi: In foreground, ALLOWED! In background, SMART_SHIELD!
        val onWifiFg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onWifiFg)

        val onWifiBg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.SMART_SHIELD, onWifiBg)
    }

    @Test
    fun testAppSearchFiltering() {
        val apps = listOf(
            AppInfo(appName = "Brave Browser", packageName = "com.brave.browser", uid = 10001, isSmartShield = true),
            AppInfo(appName = "WhatsApp", packageName = "com.whatsapp", uid = 10002, isSmartShield = true),
            AppInfo(appName = "Calculator", packageName = "com.miui.calculator", uid = 10003),
            AppInfo(appName = "YouTube", packageName = "com.google.android.youtube", uid = 10004, wifiBlocked = true)
        )

        // Case-insensitive name search
        val braveSearch = apps.filter { it.appName.contains("brave", ignoreCase = true) || it.packageName.contains("brave", ignoreCase = true) }
        assertEquals(1, braveSearch.size)
        assertEquals("Brave Browser", braveSearch[0].appName)

        // Package name search
        val packageSearch = apps.filter { it.appName.contains("google", ignoreCase = true) || it.packageName.contains("google", ignoreCase = true) }
        assertEquals(1, packageSearch.size)
        assertEquals("YouTube", packageSearch[0].appName)

        // Restricted filter
        val restrictedApps = apps.filter { it.isShielded }
        assertEquals(3, restrictedApps.size) // Brave, WhatsApp, YouTube
    }

    @Test
    fun testScheduleOvernightActiveCalculation() {
        // Bedtime schedule: 23:00 to 07:00
        val isOvernightActive = { currentHour: Int ->
            val startHour = 23
            val endHour = 7
            if (startHour > endHour) {
                currentHour >= startHour || currentHour < endHour
            } else {
                currentHour in startHour until endHour
            }
        }

        assertEquals(true, isOvernightActive(23)) // 11 PM -> Active
        assertEquals(true, isOvernightActive(0))  // Midnight -> Active
        assertEquals(true, isOvernightActive(3))  // 3 AM -> Active
        assertEquals(true, isOvernightActive(6))  // 6 AM -> Active
        assertEquals(false, isOvernightActive(7)) // 7 AM -> Inactive (ended)
        assertEquals(false, isOvernightActive(12)) // Noon -> Inactive
        assertEquals(false, isOvernightActive(22)) // 10 PM -> Inactive
    }

    @Test
    fun testMasterSwitchOffOverridesAllRestrictions() {
        // When firewall master switch is OFF, NO apps should ever be blocked
        val computeWithMasterSwitch = { serviceOn: Boolean, configuredMode: AppIsolationMode ->
            if (!serviceOn) AppIsolationMode.ALLOWED else configuredMode
        }

        // Even for Total Blackout app:
        assertEquals(AppIsolationMode.ALLOWED, computeWithMasterSwitch(false, AppIsolationMode.TOTAL_BLACKOUT))
        // Even for Smart Shield app:
        assertEquals(AppIsolationMode.ALLOWED, computeWithMasterSwitch(false, AppIsolationMode.SMART_SHIELD))
        // When ON:
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, computeWithMasterSwitch(true, AppIsolationMode.TOTAL_BLACKOUT))
        assertEquals(AppIsolationMode.SMART_SHIELD, computeWithMasterSwitch(true, AppIsolationMode.SMART_SHIELD))
    }

    @Test
    fun testMidnightQuotaResetScenario() {
        val quotaBytes = 104857600L // 100 MB
        var usedBytes = 150000000L  // 150 MB (Exceeded at 11:59 PM)

        // At 11:59 PM: Exceeded -> TOTAL_BLACKOUT
        val isExceededBeforeMidnight = (usedBytes >= quotaBytes)
        val modeBefore = if (isExceededBeforeMidnight) AppIsolationMode.TOTAL_BLACKOUT else AppIsolationMode.ALLOWED
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, modeBefore)

        // At 00:01 AM: Midnight reset occurs -> usedBytes resets to 0
        usedBytes = 0L
        val isExceededAfterMidnight = (usedBytes >= quotaBytes)
        val modeAfter = if (isExceededAfterMidnight) AppIsolationMode.TOTAL_BLACKOUT else AppIsolationMode.ALLOWED
        assertEquals(AppIsolationMode.ALLOWED, modeAfter)
    }

    @Test
    fun testNetworkHandoverWifiToCellularScenario() {
        // App has Wi-Fi blocked, Mobile Data allowed
        // 1. Phone connected to home Wi-Fi: MUST BE BLOCKED
        val onHomeWifi = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, onHomeWifi)

        // 2. User walks outside, phone switches to 4G/5G: MUST BE ALLOWED in foreground!
        val onCellularOutside = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = true,
            dataBlocked = false,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onCellularOutside)
    }

    @Test
    fun testNetworkHandoverCellularToWifiScenario() {
        // App has Mobile Data blocked (to save pack), Wi-Fi allowed
        // 1. Phone on Cellular: MUST BE BLOCKED
        val onCellular = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, onCellular)

        // 2. User connects to Wi-Fi: MUST BE ALLOWED in foreground!
        val onWifi = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = false,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.ALLOWED, onWifi)
    }

    @Test
    fun testBothNetworksBlockedUnderSmartShieldForcesBlackout() {
        // If an app is in Smart Shield, but user blocks BOTH Wi-Fi and Data:
        // It must be TOTAL_BLACKOUT in both FG and BG!
        val fg = computeEffectiveMode(
            isWifiActive = true,
            wifiBlocked = true,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = true,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, fg)

        val bg = computeEffectiveMode(
            isWifiActive = false,
            wifiBlocked = true,
            dataBlocked = true,
            isBlackout = false,
            quotaExceeded = false,
            isForeground = false,
            baseMode = AppIsolationMode.SMART_SHIELD
        )
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, bg)
    }

    @Test
    fun testFirewallActiveInactiveServiceToggle() {
        // Simulates master firewall state toggle
        var isFirewallActive = false
        val baseBlockedApps = mutableSetOf("com.instagram.android", "com.whatsapp")

        // When inactive, all Shizuku network restrictions are lifted
        fun computeAppEffectiveStatus(pkg: String): AppIsolationMode {
            if (!isFirewallActive) return AppIsolationMode.ALLOWED
            return if (baseBlockedApps.contains(pkg)) AppIsolationMode.TOTAL_BLACKOUT else AppIsolationMode.ALLOWED
        }

        assertEquals(AppIsolationMode.ALLOWED, computeAppEffectiveStatus("com.instagram.android"))
        assertEquals(AppIsolationMode.ALLOWED, computeAppEffectiveStatus("com.whatsapp"))

        // Toggle Firewall ON
        isFirewallActive = true
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, computeAppEffectiveStatus("com.instagram.android"))
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, computeAppEffectiveStatus("com.whatsapp"))
        assertEquals(AppIsolationMode.ALLOWED, computeAppEffectiveStatus("com.google.android.youtube"))

        // Toggle Firewall OFF
        isFirewallActive = false
        assertEquals(AppIsolationMode.ALLOWED, computeAppEffectiveStatus("com.instagram.android"))
        assertEquals(AppIsolationMode.ALLOWED, computeAppEffectiveStatus("com.whatsapp"))
    }

    @Test
    fun testBaseFirewallRulePersistenceAcrossMultipleSchedules() {
        // Permanent base firewall settings
        val baseFirewallModes = mutableMapOf(
            "com.facebook.katana" to AppIsolationMode.TOTAL_BLACKOUT,
            "com.twitter.android" to AppIsolationMode.SMART_SHIELD,
            "com.google.android.youtube" to AppIsolationMode.ALLOWED
        )

        // Function simulating restoreBaseFirewallRules
        fun restoreBaseRules(packages: Set<String>, currentModes: MutableMap<String, AppIsolationMode>) {
            for (pkg in packages) {
                currentModes[pkg] = baseFirewallModes[pkg] ?: AppIsolationMode.ALLOWED
            }
        }

        val activeModes = baseFirewallModes.toMutableMap()

        // Schedule 1 starts: Night Blackout on YouTube and Twitter
        val sched1Pkgs = setOf("com.google.android.youtube", "com.twitter.android")
        for (pkg in sched1Pkgs) activeModes[pkg] = AppIsolationMode.TOTAL_BLACKOUT

        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, activeModes["com.facebook.katana"])
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, activeModes["com.twitter.android"])
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, activeModes["com.google.android.youtube"])

        // Schedule 1 ends: Restore base rules for sched1Pkgs
        restoreBaseRules(sched1Pkgs, activeModes)

        // Verify base modes restored! Facebook remains TOTAL_BLACKOUT, Twitter remains SMART_SHIELD, YouTube returns to ALLOWED!
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, activeModes["com.facebook.katana"])
        assertEquals(AppIsolationMode.SMART_SHIELD, activeModes["com.twitter.android"])
        assertEquals(AppIsolationMode.ALLOWED, activeModes["com.google.android.youtube"])

        // Schedule 2 starts: Study Smart Shield on Facebook and YouTube
        val sched2Pkgs = setOf("com.facebook.katana", "com.google.android.youtube")
        for (pkg in sched2Pkgs) activeModes[pkg] = AppIsolationMode.SMART_SHIELD

        assertEquals(AppIsolationMode.SMART_SHIELD, activeModes["com.facebook.katana"])
        assertEquals(AppIsolationMode.SMART_SHIELD, activeModes["com.google.android.youtube"])

        // Schedule 2 ends: Restore base rules
        restoreBaseRules(sched2Pkgs, activeModes)

        // Facebook MUST return to TOTAL_BLACKOUT (not ALLOWED!)
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, activeModes["com.facebook.katana"])
        assertEquals(AppIsolationMode.ALLOWED, activeModes["com.google.android.youtube"])
    }

    @Test
    fun testAppConfigurationSelectorFilterAndToggleLogic() {
        val testApps = listOf(
            AppInfo("WhatsApp", "com.whatsapp", 10100),
            AppInfo("WhatsApp Business", "com.whatsapp.w4b", 10101),
            AppInfo("Instagram", "com.instagram.android", 10102),
            AppInfo("YouTube", "com.google.android.youtube", 10103)
        )

        // 1. Search Filter by name
        val searchByName = testApps.filter { it.appName.contains("whats", ignoreCase = true) }
        assertEquals(2, searchByName.size)

        // 2. Search Filter by package
        val searchByPkg = testApps.filter { it.packageName.contains("instagram", ignoreCase = true) }
        assertEquals(1, searchByPkg.size)

        // 3. Selection Toggle
        val selected = mutableSetOf<String>()
        selected.add(searchByName[0].packageName)
        selected.add(searchByName[1].packageName)
        assertEquals(2, selected.size)
        org.junit.Assert.assertTrue(selected.contains("com.whatsapp"))
        org.junit.Assert.assertTrue(selected.contains("com.whatsapp.w4b"))

        // 4. Clear All
        selected.clear()
        assertEquals(0, selected.size)

        // 5. Select All
        testApps.forEach { selected.add(it.packageName) }
        assertEquals(4, selected.size)
    }
}
