package com.smartfirewall.shizuku

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {
    private const val TAG = "ShizukuManager"

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return if (isShizukuAvailable()) {
            try {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int = 1001) {
        if (isShizukuAvailable() && !hasShizukuPermission()) {
            try {
                Shizuku.requestPermission(requestCode)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request Shizuku permission", e)
            }
        }
    }

    /**
     * Executes ADB shell command via Shizuku binder or Root shell fallback.
     */
    fun executeCommand(command: String, callback: ((Boolean, String) -> Unit)? = null): Boolean {
        return try {
            val process: Process = if (hasShizukuPermission()) {
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                ).apply { isAccessible = true }
                newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.append("ERR: ").append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val success = exitCode == 0
            Log.d(TAG, "Command: '$command' | Exit: $exitCode | Output: ${output.toString().trim()}")
            callback?.invoke(success, output.toString().trim())
            success
        } catch (e: Exception) {
            Log.e(TAG, "Failed executing command: $command", e)
            callback?.invoke(false, e.localizedMessage ?: "Execution Error")
            false
        }
    }

    /**
     * Complete multi-layer background & notification restriction:
     * - Granular independent WiFi and Mobile Data blocking via NetPolicy & AppOps
     * - Standby bucket restriction
     * - Distraction-free Notification muting
     */
    fun setAppNetworkAccess(
        packageName: String,
        uid: Int,
        blockWifi: Boolean = false,
        blockData: Boolean = false,
        blockNotifications: Boolean = true
    ): Boolean {
        val isBlocked = blockWifi || blockData
        val commands = mutableListOf<String>()

        if (isBlocked) {
            // Background execution lockdown & Doze
            commands.add("cmd appops set $packageName RUN_IN_BACKGROUND ignore")
            commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND ignore")
            commands.add("cmd appops set $packageName WAKE_LOCK ignore")
            commands.add("am set-standby-bucket $packageName restricted")
            commands.add("cmd deviceidle whitelist -$packageName")

            // Dual-Channel NetPolicy Rules
            if (blockData) {
                commands.add("cmd netpolicy add restrict-background-blacklist $uid")
                commands.add("cmd netpolicy set uid-policy $uid 1")
            }
            if (blockWifi) {
                commands.add("cmd netpolicy add restrict-background $packageName")
                commands.add("cmd netpolicy add restrict-background-blacklist $uid")
            }

            // Notification Muter
            if (blockNotifications) {
                commands.add("cmd appops set $packageName POST_NOTIFICATION ignore")
                commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS ignore")
                commands.add("pm revoke $packageName android.permission.POST_NOTIFICATIONS")
            }
        } else {
            // Restore full network connectivity and notifications
            commands.add("cmd appops set $packageName RUN_IN_BACKGROUND allow")
            commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow")
            commands.add("cmd appops set $packageName WAKE_LOCK allow")
            commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
            commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS allow")
            commands.add("pm grant $packageName android.permission.POST_NOTIFICATIONS")
            commands.add("am set-standby-bucket $packageName active")
            commands.add("cmd netpolicy remove restrict-background-blacklist $uid")
            commands.add("cmd netpolicy set uid-policy $uid 0")
            commands.add("cmd netpolicy remove restrict-background $packageName")
        }

        val combined = commands.joinToString("; ")
        return executeCommand(combined)
    }
}
