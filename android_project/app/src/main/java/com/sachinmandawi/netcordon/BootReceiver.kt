package com.sachinmandawi.netcordon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Boot or package update broadcast received: ${intent.action}")
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

            // Auto-start Floating Speedometer overlay if enabled and overlay permission granted
            try {
                if (PrefsManager.isFloatingSpeedometerEnabled(context) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(context))) {
                    FloatingSpeedometerService.start(context)
                    Log.d("BootReceiver", "FloatingSpeedometerService started after boot")
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to start FloatingSpeedometerService on boot", e)
            }

            // Restore all active firewall schedules
            try {
                ScheduleReceiver.rescheduleAll(context)
                Log.d("BootReceiver", "Firewall schedules restored after boot")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to restore schedules on boot", e)
            }
        }
    }
}
