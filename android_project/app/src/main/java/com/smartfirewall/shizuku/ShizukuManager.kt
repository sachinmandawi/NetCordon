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
     * 1. AppOps RUN_IN_BACKGROUND + RUN_ANY_IN_BACKGROUND -> ignore
     * 2. AppOps WAKE_LOCK -> ignore
     * 3. Standby bucket -> restricted
     * 4. NetPolicy -> restrict-background-blacklist & uid-policy 1
     * 5. Notification blocking -> AppOps POST_NOTIFICATION ignore & ACCESS_NOTIFICATIONS ignore & pm revoke
     */
    fun setAppNetworkAccess(packageName: String, uid: Int, block: Boolean, blockNotifications: Boolean = true): Boolean {
        val commands = if (block) {
            val list = mutableListOf(
                "cmd appops set $packageName RUN_IN_BACKGROUND ignore",
                "cmd appops set $packageName RUN_ANY_IN_BACKGROUND ignore",
                "cmd appops set $packageName WAKE_LOCK ignore",
                "am set-standby-bucket $packageName restricted",
                "cmd netpolicy add restrict-background-blacklist $uid",
                "cmd netpolicy set uid-policy $uid 1",
                "cmd netpolicy add restrict-background $packageName",
                "cmd deviceidle whitelist -$packageName"
            )
            if (blockNotifications) {
                list.add("cmd appops set $packageName POST_NOTIFICATION ignore")
                list.add("cmd appops set $packageName ACCESS_NOTIFICATIONS ignore")
                list.add("pm revoke $packageName android.permission.POST_NOTIFICATIONS")
            }
            list
        } else {
            listOf(
                "cmd appops set $packageName RUN_IN_BACKGROUND allow",
                "cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow",
                "cmd appops set $packageName WAKE_LOCK allow",
                "cmd appops set $packageName POST_NOTIFICATION allow",
                "cmd appops set $packageName ACCESS_NOTIFICATIONS allow",
                "pm grant $packageName android.permission.POST_NOTIFICATIONS",
                "am set-standby-bucket $packageName active",
                "cmd netpolicy remove restrict-background-blacklist $uid",
                "cmd netpolicy set uid-policy $uid 0",
                "cmd netpolicy remove restrict-background $packageName"
            )
        }

        val combined = commands.joinToString("; ")
        return executeCommand(combined)
    }
}
