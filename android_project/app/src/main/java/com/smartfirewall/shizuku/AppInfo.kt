package com.smartfirewall.shizuku

import androidx.compose.ui.graphics.ImageBitmap

data class AppInfo(
    val appName: String,
    val packageName: String,
    val uid: Int,
    val iconBitmap: ImageBitmap? = null,
    var wifiBlocked: Boolean = false,
    var dataBlocked: Boolean = false,
    var currentPolicy: Int = 0 // 0 = ALLOW (Internet ON), 1 = REJECT_ALL (Internet OFF)
) {
    val isShielded: Boolean get() = wifiBlocked || dataBlocked
}
