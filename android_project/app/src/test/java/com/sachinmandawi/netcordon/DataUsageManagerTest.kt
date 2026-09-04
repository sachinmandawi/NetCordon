package com.sachinmandawi.netcordon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataUsageManagerTest {

    @Test
    fun testFormatBytesZeroAndNegative() {
        assertEquals("0 B", DataUsageManager.formatBytes(0))
        assertEquals("0 B", DataUsageManager.formatBytes(-500))
    }

    @Test
    fun testFormatBytesBytes() {
        assertEquals("512 B", DataUsageManager.formatBytes(512))
        assertEquals("1023 B", DataUsageManager.formatBytes(1023))
    }

    @Test
    fun testFormatBytesKilobytes() {
        assertEquals("1.0 KB", DataUsageManager.formatBytes(1024))
        assertEquals("10.5 KB", DataUsageManager.formatBytes((10.5 * 1024).toLong()))
    }

    @Test
    fun testFormatBytesMegabytes() {
        assertEquals("1.0 MB", DataUsageManager.formatBytes(1024 * 1024))
        assertEquals("500.0 MB", DataUsageManager.formatBytes(500L * 1024 * 1024))
    }

    @Test
    fun testFormatBytesGigabytes() {
        assertEquals("1.00 GB", DataUsageManager.formatBytes(1024L * 1024 * 1024))
        assertEquals("2.50 GB", DataUsageManager.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testGetTimeRangeToday() {
        val (start, end) = DataUsageManager.getTimeRange("today")
        assertTrue(start <= end)
        assertTrue(end - start <= 24L * 60 * 60 * 1000)
    }

    @Test
    fun testGetTimeRangeWeek() {
        val (start, end) = DataUsageManager.getTimeRange("week")
        val diff = end - start
        val expected = 7L * 24 * 60 * 60 * 1000
        assertEquals(expected, diff)
    }

    @Test
    fun testGetTimeRangeMonth() {
        val (start, end) = DataUsageManager.getTimeRange("month")
        val diff = end - start
        val expected = 30L * 24 * 60 * 60 * 1000
        assertEquals(expected, diff)
    }

    @Test
    fun testAppUsageStatsSummations() {
        val stats = AppUsageStats(
            packageName = "com.test.app",
            appName = "Test App",
            uid = 10100,
            rxWifi = 1000L,
            txWifi = 500L,
            rxMobile = 2000L,
            txMobile = 1000L
        )
        assertEquals(1500L, stats.totalWifi)
        assertEquals(3000L, stats.totalMobile)
        assertEquals(4500L, stats.totalBytes)
    }
}
