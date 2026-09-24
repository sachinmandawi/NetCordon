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

    private val newProcessMethod by lazy {
        Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).apply { isAccessible = true }
    }

    /**
     * Executes ADB shell command via Shizuku binder or Root shell fallback.
     */
    fun executeCommand(command: String, callback: ((Boolean, String) -> Unit)? = null): Boolean {
        return try {
            val process: Process = if (hasShizukuPermission()) {
                newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            }

            val output = StringBuilder()
            val readerThread = Thread {
                try {
                    val outText = process.inputStream.bufferedReader().use { it.readText() }
                    if (outText.isNotEmpty()) output.append(outText)
                    if (output.isEmpty()) {
                        val errText = process.errorStream.bufferedReader().use { it.readText() }
                        if (errText.isNotEmpty()) output.append(errText)
                    }
                } catch (_: Exception) {
                }
            }.apply {
                isDaemon = true
                start()
            }

            val finished = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                process.waitFor(4, java.util.concurrent.TimeUnit.SECONDS)
            } else {
                process.waitFor() == 0
            }

            if (!finished) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly()
                } else {
                    process.destroy()
                }
            } else {
                readerThread.join(500)
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
        val cmds = mutableListOf<String>()
        if (!packageName.isNullOrBlank()) {
            cmds.add("cmd appops set $packageName GET_USAGE_STATS allow")
            cmds.add("pm grant $packageName android.permission.PACKAGE_USAGE_STATS")
        }
        cmds.add("cmd connectivity set-chain3-enabled true")
        return executeCommand(cmds.joinToString("; "))
    }

    data class BatchAppRule(
        val packageName: String,
        val uid: Int,
        val mode: AppIsolationMode,
        val isForeground: Boolean = false
    )

    fun buildAppIsolationCommands(
        packageName: String,
        uid: Int,
        mode: AppIsolationMode,
        isForeground: Boolean = false,
        blockNotifications: Boolean = true,
        includeChain3Enable: Boolean = true
    ): List<String> {
        val commands = mutableListOf<String>()

        when (mode) {
            AppIsolationMode.TOTAL_BLACKOUT -> {
                if (includeChain3Enable) {
                    commands.add("cmd connectivity set-chain3-enabled true")
                }
                commands.add("cmd connectivity set-package-networking-enabled false $packageName")
                commands.add("cmd netpolicy add restrict-background-blacklist $uid")
                commands.add("cmd appops set $packageName RUN_IN_BACKGROUND ignore")
                commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND ignore")
                commands.add("cmd appops set $packageName WAKE_LOCK ignore")
                commands.add("am set-standby-bucket $packageName restricted")
                commands.add("cmd deviceidle whitelist -$packageName")

                if (blockNotifications) {
                    commands.add("cmd appops set $packageName POST_NOTIFICATION ignore")
                    commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS ignore")
                } else {
                    commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
                    commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS allow")
                }

                commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; iptables -I OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -I OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
            }
            AppIsolationMode.SMART_SHIELD -> {
                if (isForeground) {
                    commands.add("cmd connectivity set-package-networking-enabled true $packageName")
                    commands.add("cmd netpolicy set-uid-policy $uid 0")
                    commands.add("cmd netpolicy remove restrict-background-blacklist $uid")
                    commands.add("cmd appops set $packageName RUN_IN_BACKGROUND allow")
                    commands.add("cmd appops set $packageName RUN_ANY_IN_BACKGROUND allow")
                    commands.add("cmd appops set $packageName WAKE_LOCK allow")
                    commands.add("am set-standby-bucket $packageName active")
                    commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
                    commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS allow")
                    commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
                } else {
                    commands.add("cmd connectivity set-package-networking-enabled true $packageName")
                    commands.add("iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null")
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
                    } else {
                        commands.add("cmd appops set $packageName POST_NOTIFICATION allow")
                        commands.add("cmd appops set $packageName ACCESS_NOTIFICATIONS allow")
                    }
                }
            }
            AppIsolationMode.ALLOWED -> {
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
        return commands
    }

    fun applyBatchIsolationRules(
        rules: List<BatchAppRule>,
        blockNotifications: Boolean = true,
        enableChain3: Boolean = true
    ): Boolean {
        if (rules.isEmpty()) return true
        val allCommands = mutableListOf<String>()
        if (enableChain3) {
            allCommands.add("cmd connectivity set-chain3-enabled true")
        }
        for (rule in rules) {
            allCommands.addAll(
                buildAppIsolationCommands(
                    packageName = rule.packageName,
                    uid = rule.uid,
                    mode = rule.mode,
                    isForeground = rule.isForeground,
                    blockNotifications = blockNotifications,
                    includeChain3Enable = false
                )
            )
        }
        return executeCommand(allCommands.joinToString("; "))
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
        val commands = buildAppIsolationCommands(
            packageName = packageName,
            uid = uid,
            mode = mode,
            isForeground = isForeground,
            blockNotifications = blockNotifications,
            includeChain3Enable = true
        )
        return executeCommand(commands.joinToString("; "))
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
