package com.sachinmandawi.netcordon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppInfoTest {

    @Test
    fun testAppInfoDefaultState() {
        val app = AppInfo(appName = "YouTube", packageName = "com.google.android.youtube", uid = 10150)
        assertFalse(app.wifiBlocked)
        assertFalse(app.dataBlocked)
        assertFalse(app.isBlackout)
        assertFalse(app.isShielded)
        assertEquals(AppIsolationMode.ALLOWED, app.isolationMode)
    }

    @Test
    fun testAppInfoWifiBlockedState() {
        val app = AppInfo(appName = "YouTube", packageName = "com.google.android.youtube", uid = 10150, wifiBlocked = true)
        assertTrue(app.wifiBlocked)
        assertFalse(app.dataBlocked)
        assertTrue(app.isShielded)
        assertEquals(AppIsolationMode.SMART_SHIELD, app.isolationMode)
    }

    @Test
    fun testAppInfoDataBlockedState() {
        val app = AppInfo(appName = "Chrome", packageName = "com.android.chrome", uid = 10160, dataBlocked = true)
        assertFalse(app.wifiBlocked)
        assertTrue(app.dataBlocked)
        assertTrue(app.isShielded)
        assertEquals(AppIsolationMode.SMART_SHIELD, app.isolationMode)
    }

    @Test
    fun testAppInfoSmartShieldState() {
        val app = AppInfo(appName = "WhatsApp", packageName = "com.whatsapp", uid = 10180, isSmartShield = true)
        assertFalse(app.wifiBlocked)
        assertFalse(app.dataBlocked)
        assertFalse(app.isBlackout)
        assertTrue(app.isSmartShield)
        assertTrue(app.isShielded)
        assertEquals(AppIsolationMode.SMART_SHIELD, app.isolationMode)
    }

    @Test
    fun testAppInfoTotalBlackoutState() {
        val app = AppInfo(appName = "AdWare", packageName = "com.evil.adware", uid = 10170, isBlackout = true)
        assertTrue(app.isShielded)
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, app.isolationMode)
    }
}
