package com.sachinmandawi.netcordon

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val appName: String,
    val packageName: String,
    val uid: Int,
    val iconBitmap: ImageBitmap? = null,
    var wifiBlocked: Boolean = false,
    var dataBlocked: Boolean = false,
    var isBlackout: Boolean = false,
    var isSmartShield: Boolean = false,
    var currentPolicy: Int = 0, // 0 = ALLOW (Internet ON), 1 = REJECT_ALL (Internet OFF)
    var dailyQuotaBytes: Long = 0L
) {
    val isShielded: Boolean get() = isSmartShield || wifiBlocked || dataBlocked || isBlackout
    val baseIsolationMode: AppIsolationMode get() = when {
        isBlackout -> AppIsolationMode.TOTAL_BLACKOUT
        isSmartShield -> AppIsolationMode.SMART_SHIELD
        else -> AppIsolationMode.ALLOWED
    }
    val isolationMode: AppIsolationMode get() = when {
        isBlackout -> AppIsolationMode.TOTAL_BLACKOUT
        isSmartShield -> AppIsolationMode.SMART_SHIELD
        wifiBlocked || dataBlocked -> AppIsolationMode.SMART_SHIELD
        else -> AppIsolationMode.ALLOWED
    }
}
