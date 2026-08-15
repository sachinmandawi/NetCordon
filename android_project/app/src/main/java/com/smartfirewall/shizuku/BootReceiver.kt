package com.smartfirewall.shizuku

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completed broadcast received")
            if (PrefsManager.isStartOnBoot(context) && PrefsManager.isServiceEnabled(context)) {
                val serviceIntent = Intent(context, AppShieldService::class.java).apply {
                    action = AppShieldService.ACTION_START
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Log.d("BootReceiver", "AppShieldService started after boot")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to start service on boot", e)
                }
            }
        }
    }
}
