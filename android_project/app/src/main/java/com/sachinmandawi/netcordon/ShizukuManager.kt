package com.sachinmandawi.netcordon

import android.content.pm.PackageManager
import android.os.Build
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

            val finished = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
            } else {
                process.waitFor() == 0
            }

            val output = StringBuilder()
            if (finished) {
                try {
                    val outText = process.inputStream.bufferedReader().use { it.readText() }
                    if (outText.isNotEmpty()) output.append(outText)
                    if (output.isEmpty()) {
                        val errText = process.errorStream.bufferedReader().use { it.readText() }
                        if (errText.isNotEmpty()) output.append(errText)
                    }
                } catch (e: Exception) {
                    // stream closed
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly()
                } else {
                    process.destroy()
                }
            }

            val exitCode = if (finished) process.exitValue() else -1
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
     * Initializes Android 11+ Native Connectivity Chain-3 Firewall framework
     * and grants required system privileges (Usage Stats, AppOps).
     */
    fun initFirewallFramework(packageName: String? = null): Boolean {
        if (!packageName.isNullOrBlank()) {
            executeCommand("cmd appops set $packageName GET_USAGE_STATS allow")
            executeCommand("pm grant $packageName android.permission.PACKAGE_USAGE_STATS")
        }
        return executeCommand("cmd connectivity set-chain3-enabled true")
    }

    /**
     * Complete multi-layer network isolation engine:
     * 1. Android 11+ Native Connectivity Chain-3 Firewall (set-package-networking-enabled)
     * 2. Android NetPolicy Background & Metered Blacklist (cmd netpolicy)
     * 3. AppOps Background Execution, Wakelock & Notification Freeze (cmd appops)
     * 4. Standby Bucket Restriction (am set-standby-bucket)
     * 5. Root IPTables & IP6Tables HARD DROP fallback for rooted environments
     */
    fun setAppIsolation(
        packageName: String,
        uid: Int,
        mode: AppIsolationMode,
        isForeground: Boolean = false,
        blockNotifications: Boolean = true
    ): Boolean {
        val commands = mutableListOf<String>()

        when (mode) {
            AppIsolationMode.TOTAL_BLACKOUT -> {
                // Android 11+ Native Chain-3 Firewall (Instant 0 KB/s in foreground & background)
                commands.add("cmd connectivity set-chain3-enabled true")
                commands.add("cmd connectivity set-package-networking-enabled false $packageName")

                // NetPolicy Restrictions
                commands.add("cmd netpolicy add restrict-background-blacklist $uid")

                // AppOps execution lockdown
                commands.add("cmd appops set $packageName RUN_IN_BACKGROUND ignore")
                commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND ignore")
                commands.add("cmd appops set $packageName WAKE_LOCK ignore")
                commands.add("am set-standby-bucket $packageName restricted")
                commands.add("cmd deviceidle whitelist -$packageName")

                // Mute notifications
                if (blockNotifications) {
                    commands.add("cmd appops set $packageName POST_NOTIFICATION ignore")
                    commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS ignore")
                }

                // IPTables & IP6Tables root drop fallback (Dual-stack IPv4/IPv6 protection)
                commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; iptables -I OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -I OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
            }
            AppIsolationMode.SMART_SHIELD -> {
                if (isForeground) {
                    // App is active on screen: Instantly restore full connectivity
                    commands.add("cmd connectivity set-package-networking-enabled true $packageName")
                    commands.add("cmd netpolicy set-uid-policy $uid 0")
                    commands.add("cmd netpolicy remove restrict-background-blacklist $uid")
                    commands.add("cmd appops set $packageName RUN_IN_BACKGROUND allow")
                    commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow")
                    commands.add("cmd appops set $packageName WAKE_LOCK allow")
                    commands.add("am set-standby-bucket $packageName active")
                    commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
                    commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
                } else {
                    // App is closed / in background: Freeze network and wakeups (Single Tick ✓)
                    // Keep networking enabled in connectivity manager so foreground launch never suffers socket failure
                    commands.add("cmd connectivity set-package-networking-enabled true $packageName")
                    // Clean up any lingering full drop rules from previous Total Blackout
                    commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
                    // Native Android background restrict (kernel drops packets ONLY when backgrounded)
                    commands.add("cmd netpolicy set-uid-policy $uid 1")
                    commands.add("cmd netpolicy add restrict-background-blacklist $uid")
                    commands.add("cmd appops set $packageName RUN_IN_BACKGROUND ignore")
                    commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND ignore")
                    commands.add("cmd appops set $packageName WAKE_LOCK ignore")
                    commands.add("am set-standby-bucket $packageName restricted")
                    commands.add("cmd deviceidle whitelist -$packageName")
                    if (blockNotifications) {
                        commands.add("cmd appops set $packageName POST_NOTIFICATION ignore")
                        commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS ignore")
                    }
                }
            }
            AppIsolationMode.ALLOWED -> {
                // Restore 100% Unrestricted Connectivity
                commands.add("cmd connectivity set-package-networking-enabled true $packageName")
                commands.add("cmd netpolicy set-uid-policy $uid 0")
                commands.add("cmd netpolicy remove restrict-background-blacklist $uid")
                commands.add("cmd appops set $packageName RUN_IN_BACKGROUND allow")
                commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow")
                commands.add("cmd appops set $packageName WAKE_LOCK allow")
                commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
                commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS allow")
                commands.add("am set-standby-bucket $packageName active")
                commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
            }
        }

        val combined = commands.joinToString("; ")
        return executeCommand(combined)
    }

    /**
     * Backward compatibility helper for legacy calls.
     */
    fun setAppNetworkAccess(
        packageName: String,
        uid: Int,
        blockWifi: Boolean = false,
        blockData: Boolean = false,
        blockNotifications: Boolean = true
    ): Boolean {
        val mode = when {
            blockWifi && blockData -> AppIsolationMode.SMART_SHIELD
            blockWifi || blockData -> AppIsolationMode.SMART_SHIELD
            else -> AppIsolationMode.ALLOWED
        }
        return setAppIsolation(
            packageName = packageName,
            uid = uid,
            mode = mode,
            isForeground = false,
            blockNotifications = blockNotifications
        )
    }
}
