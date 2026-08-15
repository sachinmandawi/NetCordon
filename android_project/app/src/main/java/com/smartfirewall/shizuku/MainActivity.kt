package com.smartfirewall.shizuku

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/* ─── Minimal Dark Palette ─── */
private val BG      = Color(0xFF141414)
private val Surf    = Color(0xFF1E1E1E)
private val Surf2   = Color(0xFF282828)
private val Div     = Color(0xFF262626)
private val OnBg    = Color(0xFFECECEC)
private val Dim     = Color(0xFF888888)
private val Green   = Color(0xFF4CAF50)
private val Red     = Color(0xFFEF5350)
private val Yellow  = Color(0xFFFFB300)
private val Blue    = Color(0xFF64B5F6)
private val TBg     = Color(0xFF1A1A1A)

/* ─── Dark ColorScheme with Green Primary (Zero Blue Ripple/Highlights) ─── */
private val NetCordonColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A20),
    onPrimaryContainer = Color(0xFF81C784),
    secondary = Green,
    onSecondary = Color.White,
    secondaryContainer = Surf,
    onSecondaryContainer = OnBg,
    tertiary = Green,
    background = BG,
    onBackground = OnBg,
    surface = Surf,
    onSurface = OnBg,
    surfaceVariant = Surf2,
    onSurfaceVariant = Dim,
    outline = Div
)

/* ─── Custom Clean Minimalist Switch (Zero Blue Ripple / Zero Square Artifacts) ─── */
@Composable
fun NetCordonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) Green else Color(0xFF333333),
        label = "switchTrack"
    )
    val thumbOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/* ─── Animated Splash Screen (Native Jetpack Compose) ─── */
@Composable
fun AnimatedSplashScreen(onFinish: () -> Unit) {
    val logoScale = remember { Animatable(0.7f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(18f) }
    val subAlpha = remember { Animatable(0f) }
    val progressBar = remember { Animatable(0f) }
    val containerAlpha = remember { Animatable(1f) }
    val containerScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // 1. Spring scale logo immediately
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // 2. Title fade and slide up
        launch {
            delay(120)
            titleAlpha.animateTo(1f, tween(600))
        }
        launch {
            delay(120)
            titleOffsetY.animateTo(0f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
        }

        // 3. Subtitle fade
        launch {
            delay(240)
            subAlpha.animateTo(1f, tween(600))
        }

        // 4. Progress bar fill smoothly across 2.4s
        launch {
            delay(80)
            progressBar.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
            )
        }

        // Hold on screen for full 2.8s
        delay(2800)

        // 5. Exit fade & scale transition
        launch {
            containerAlpha.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            containerScale.animateTo(1.03f, tween(450, easing = FastOutSlowInEasing))
        }
        delay(450)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .graphicsLayer {
                alpha = containerAlpha.value
                scaleX = containerScale.value
                scaleY = containerScale.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Flat App Logo (Spring Scale)
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "NetCordon",
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(Modifier.height(20.dp))

            // App Title (Slide Up & Fade)
            Text(
                text = "NetCordon",
                color = Color(0xFFF0F0F0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffsetY.value
                }
            )

            Spacer(Modifier.height(6.dp))

            // Subtitle (Staggered Fade)
            Text(
                text = "Intelligent App Firewall & Shield",
                color = Dim,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = subAlpha.value
                }
            )

            Spacer(Modifier.height(28.dp))

            // Smooth Progress Bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF262626))
                    .graphicsLayer { alpha = subAlpha.value }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((140 * progressBar.value).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Green)
                )
            }
        }
    }
}
class MainActivity : ComponentActivity() {

    private val shizukuPermListener = Shizuku.OnRequestPermissionResultListener { _, _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(shizukuPermListener)
        setContent {
            MaterialTheme(colorScheme = NetCordonColorScheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = BG
                ) { NetCordonApp() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermListener)
    }
}

/* ─── Helper: Usage Access check ─── */
fun hasUsageAccess(ctx: Context): Boolean = try {
    val ops = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
    else
        @Suppress("DEPRECATION")
        ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.packageName)
    mode == AppOpsManager.MODE_ALLOWED
} catch (e: Exception) { false }

/* ─── Helper: Notification permission ─── */
fun hasNotificationPerm(ctx: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    else true

/* ─── Helper: fetch installed user apps ─── */
fun fetchInstalledApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val savedWifi = PrefsManager.getWifiBlockedPackages(ctx)
    val savedData = PrefsManager.getDataBlockedPackages(ctx)
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { ai ->
            val iconBmp = try {
                pm.getApplicationIcon(ai).toBitmap(width = 96, height = 96).asImageBitmap()
            } catch (e: Exception) {
                null
            }
            AppInfo(
                appName     = pm.getApplicationLabel(ai).toString(),
                packageName = ai.packageName,
                uid         = ai.uid,
                iconBitmap  = iconBmp,
                wifiBlocked = savedWifi.contains(ai.packageName),
                dataBlocked = savedData.contains(ai.packageName)
            )
        }
        .sortedBy { it.appName.lowercase() }
}

/* ═══════════════════════════════════════════════════
   ROOT COMPOSABLE
═══════════════════════════════════════════════════ */
@Composable
fun NetCordonApp() {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /* ── Splash Screen ── */
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        AnimatedSplashScreen(onFinish = { showSplash = false })
        return
    }

    /* ── Permission live state ── */
    var shizukuOk  by remember { mutableStateOf(ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) }
    var usageOk    by remember { mutableStateOf(hasUsageAccess(ctx)) }
    var notifOk    by remember { mutableStateOf(hasNotificationPerm(ctx)) }
    var batteryOk  by remember { mutableStateOf(false) }

    val allRequired = shizukuOk && usageOk && notifOk
    var setupDone by remember { mutableStateOf(PrefsManager.isSetupCompleted(ctx) && allRequired) }

    /* ── Launchers ── */
    val usageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        usageOk = hasUsageAccess(ctx)
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifOk = it
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        batteryOk = true
    }

    /* ── Navigation ── */
    var screen      by remember { mutableStateOf("home") } // home | logs | settings
    var searching   by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var menuOpen    by remember { mutableStateOf(false) }

    /* ── App list (Loaded asynchronously on IO thread) ── */
    var apps by remember { mutableStateOf(listOf<AppInfo>()) }
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { fetchInstalledApps(ctx) }
        apps = loaded
        val uidMap = loaded.associate { it.packageName to it.uid }
        PrefsManager.savePackageUidMap(ctx, uidMap)
    }

    /* ── Settings (loaded from persistent PrefsManager) ── */
    var privilege        by remember { mutableStateOf(PrefsManager.getPrivilege(ctx)) }
    var bootStart        by remember { mutableStateOf(PrefsManager.isStartOnBoot(ctx)) }
    var serviceOn        by remember { mutableStateOf(PrefsManager.isServiceEnabled(ctx)) }
    var showPkg          by remember { mutableStateOf(PrefsManager.isShowPkg(ctx)) }
    var minNotif         by remember { mutableStateOf(PrefsManager.isMinNotif(ctx)) }
    var blockNotifs      by remember { mutableStateOf(PrefsManager.isBlockNotifications(ctx)) }
    var screenOffShield  by remember { mutableStateOf(PrefsManager.isScreenOffShield(ctx)) }

    /* ── Service sync helper ── */
    fun syncService(updatedApps: List<AppInfo>) {
        val wifiPkgs = updatedApps.filter { it.wifiBlocked }.map { it.packageName }.toSet()
        val dataPkgs = updatedApps.filter { it.dataBlocked }.map { it.packageName }.toSet()
        val uidMap   = updatedApps.associate { it.packageName to it.uid }

        PrefsManager.saveWifiBlockedPackages(ctx, wifiPkgs)
        PrefsManager.saveDataBlockedPackages(ctx, dataPkgs)
        PrefsManager.savePackageUidMap(ctx, uidMap)

        val shielded = updatedApps.filter { it.wifiBlocked || it.dataBlocked }
        if (!serviceOn) return
        val intent = Intent(ctx, AppShieldService::class.java).apply {
            action = AppShieldService.ACTION_UPDATE
            putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded.map { it.packageName }))
            putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.map { it.uid }))
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(intent)
            else ctx.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* ── Logs ── */
    val logs = remember { mutableStateListOf(
        "${timestamp()}  I  Shizuku: Binder connected",
        "${timestamp()}  D  Auth: Permission granted",
        "${timestamp()}  I  Service: NetCordon monitoring active"
    )}
    fun pushLog(line: String) { logs.add(0, line); if (logs.size > 100) logs.removeAt(logs.lastIndex) }

    /* ════════════════════════════════
       SETUP / PERMISSION SCREEN
       (NO-SCROLL, ZERO CLUTTER)
    ════════════════════════════════ */
    if (!setupDone) {
        CompactSetupScreen(
            shizukuOk = shizukuOk, usageOk = usageOk, notifOk = notifOk, batteryOk = batteryOk,
            onGrantShizuku = {
                Shizuku.requestPermission(1001)
                shizukuOk = ShizukuManager.hasShizukuPermission()
            },
            onGrantUsage   = { usageLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
            onGrantNotif   = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                else notifOk = true
            },
            onGrantBattery = {
                batteryLauncher.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${ctx.packageName}")))
            },
            onContinue = {
                PrefsManager.setSetupCompleted(ctx, true)
                setupDone = true
                val shielded = apps.filter { it.wifiBlocked || it.dataBlocked }
                val intent = Intent(ctx, AppShieldService::class.java).apply {
                    action = AppShieldService.ACTION_START
                    putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded.map { it.packageName }))
                    putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.map { it.uid }))
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(intent)
                    else ctx.startService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
        return
    }

    /* ════════════════════════════════
       MAIN APP (AFTER SETUP)
    ════════════════════════════════ */
    /* ── App list (User-installed apps only) ── */
    val filtered = apps.filter {
        it.appName.contains(searchQuery, true) || it.packageName.contains(searchQuery, true)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        /* ── Toolbar ── */
        when (screen) {
            "home" -> {
                Column(modifier = Modifier.background(TBg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searching) {
                            IconButton(onClick = { searching = false; searchQuery = "" }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnBg)
                            }
                            TextField(
                                value = searchQuery, onValueChange = { searchQuery = it },
                                placeholder = { Text("Search applications…", color = Dim, fontSize = 16.sp) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = OnBg, unfocusedTextColor = OnBg,
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null, tint = Dim)
                                }
                            }
                        } else {
                            Spacer(Modifier.width(16.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "NetCordon",
                                modifier = Modifier.size(26.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("NetCordon", color = OnBg, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            
                            // ── Master ON/OFF Switch (NetGuard style) ──
                            NetCordonSwitch(
                                checked = serviceOn,
                                onCheckedChange = { newState ->
                                    serviceOn = newState
                                    PrefsManager.setServiceEnabled(ctx, newState)
                                    if (newState) {
                                        val shielded = apps.filter { it.wifiBlocked || it.dataBlocked }
                                        val intent = Intent(ctx, AppShieldService::class.java).apply {
                                            action = AppShieldService.ACTION_START
                                            putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded.map { it.packageName }))
                                            putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.map { it.uid }))
                                        }
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(intent)
                                            else ctx.startService(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        pushLog("${timestamp()}  I  Service: Master switch ON — Firewall active")
                                    } else {
                                        val intent = Intent(ctx, AppShieldService::class.java).apply {
                                            action = AppShieldService.ACTION_STOP
                                        }
                                        try {
                                            ctx.startService(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        pushLog("${timestamp()}  W  Service: Master switch OFF — Restrictions paused")
                                    }
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            IconButton(onClick = { searching = true }) {
                                Icon(Icons.Default.Search, null, tint = OnBg.copy(alpha = 0.8f))
                            }
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, null, tint = OnBg.copy(alpha = 0.8f))
                                }
                                DropdownMenu(
                                    expanded = menuOpen,
                                    onDismissRequest = { menuOpen = false },
                                    modifier = Modifier.background(Surf2)
                                ) {
                                    listOf(
                                        "Logs"     to { screen = "logs" },
                                        "Settings" to { screen = "settings" }
                                    ).forEach { (label, action) ->
                                        DropdownMenuItem(
                                            text = { Text(label, color = OnBg, fontSize = 14.sp) },
                                            onClick = { action(); menuOpen = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Div)
                }
            }
            "logs" -> SubToolbar("Logs", onBack = { screen = "home" }) {
                IconButton(onClick = { logs.clear() }) {
                    Icon(Icons.Default.Delete, null, tint = OnBg.copy(alpha = 0.8f))
                }
            }
            "settings" -> SubToolbar("Settings", onBack = { screen = "home" })
        }

        /* ── Native Android System Back Navigation ── */
        BackHandler(enabled = searching || screen != "home" || menuOpen) {
            when {
                menuOpen -> menuOpen = false
                searching -> {
                    searching = false
                    searchQuery = ""
                }
                screen != "home" -> screen = "home"
            }
        }

        /* ── Screen Content ── */
        when (screen) {
            "home" -> HomeContent(
            apps           = filtered,
            showPkg        = showPkg,
            shizukuOk      = shizukuOk,
            serviceOn      = serviceOn,
            onShizukuRetry = { shizukuOk = ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission() },
            onToggleWifi = { id ->
                val nextApps = apps.map { if (it.packageName != id) it else it.copy(wifiBlocked = !it.wifiBlocked) }
                apps = nextApps
                val target = nextApps.firstOrNull { it.packageName == id }
                if (target != null) {
                    if (target.wifiBlocked) {
                        pushLog("${timestamp()}  W  NetPol: ${target.appName} WiFi -> BLOCKED (\\)")
                    } else {
                        pushLog("${timestamp()}  I  NetPol: ${target.appName} WiFi -> ALLOWED")
                    }
                    ShizukuManager.setAppNetworkAccess(
                        packageName = target.packageName,
                        uid = target.uid,
                        blockWifi = target.wifiBlocked,
                        blockData = target.dataBlocked,
                        blockNotifications = blockNotifs
                    )
                }
                syncService(nextApps)
            },
            onToggleData = { id ->
                val nextApps = apps.map { if (it.packageName != id) it else it.copy(dataBlocked = !it.dataBlocked) }
                apps = nextApps
                val target = nextApps.firstOrNull { it.packageName == id }
                if (target != null) {
                    if (target.dataBlocked) {
                        pushLog("${timestamp()}  W  NetPol: ${target.appName} Mobile Data -> BLOCKED (\\)")
                    } else {
                        pushLog("${timestamp()}  I  NetPol: ${target.appName} Mobile Data -> ALLOWED")
                    }
                    ShizukuManager.setAppNetworkAccess(
                        packageName = target.packageName,
                        uid = target.uid,
                        blockWifi = target.wifiBlocked,
                        blockData = target.dataBlocked,
                        blockNotifications = blockNotifs
                    )
                }
                syncService(nextApps)
            }
        )
        "logs"     -> LogsScreen(logs)
        "settings" -> SettingsScreen(
            privilege = privilege, bootStart = bootStart, serviceOn = serviceOn,
            showPkg = showPkg, minNotif = minNotif, blockNotifs = blockNotifs, screenOffShield = screenOffShield,
            shizukuOk = shizukuOk, usageOk = usageOk, notifOk = notifOk, batteryOk = batteryOk,
            onPrivilege = { privilege = it; PrefsManager.setPrivilege(ctx, it) },
            onBoot      = { bootStart = it; PrefsManager.setStartOnBoot(ctx, it) },
            onService   = { v ->
                serviceOn = v
                PrefsManager.setServiceEnabled(ctx, v)
                val intent = Intent(ctx, AppShieldService::class.java).apply {
                    action = if (v) AppShieldService.ACTION_START else AppShieldService.ACTION_STOP
                    val shielded = apps.filter { it.wifiBlocked || it.dataBlocked }
                    putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded.map { it.packageName }))
                    putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.map { it.uid }))
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(intent)
                else ctx.startService(intent)
                pushLog("${timestamp()}  I  Service: AppShieldService ${if (v) "started" else "stopped"}")
            },
            onPkg     = { showPkg = it; PrefsManager.setShowPkg(ctx, it) },
            onNotif   = { minNotif = it; PrefsManager.setMinNotif(ctx, it) },
            onBlockNotifs = { v ->
                blockNotifs = v
                PrefsManager.setBlockNotifications(ctx, v)
                syncService(apps)
            },
            onScreenOffShield = { v ->
                screenOffShield = v
                PrefsManager.setScreenOffShield(ctx, v)
                pushLog("${timestamp()}  I  Rule: Screen Lock Auto-Shield ${if (v) "Enabled" else "Disabled"}")
            },
            onGrantUsage   = { usageLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
            onGrantNotif   = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) else notifOk = true },
            onGrantBattery = { batteryLauncher.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))) },
            onGrantShizuku = { Shizuku.requestPermission(1001) }
        )
    }
}
}

/* ─── Timestamp helper ─── */
fun timestamp(): String {
    return java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
}

/* ─── Sub-screen Toolbar ─── */
@Composable
fun SubToolbar(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Column(modifier = Modifier.background(TBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnBg)
            }
            Text(title, color = OnBg, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        HorizontalDivider(color = Div)
    }
}

/* ═══════════════════════════════════════════════════
   ULTRA-SLEEK ZERO-SCROLL SETUP SCREEN (NO CLUTTER)
═══════════════════════════════════════════════════ */
@Composable
fun CompactSetupScreen(
    shizukuOk: Boolean, usageOk: Boolean, notifOk: Boolean, batteryOk: Boolean,
    onGrantShizuku: () -> Unit, onGrantUsage: () -> Unit, onGrantNotif: () -> Unit, onGrantBattery: () -> Unit,
    onContinue: () -> Unit
) {
    val allRequired = shizukuOk && usageOk && notifOk

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top: Minimal Hero ──
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "NetCordon",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "App Permissions",
                color = OnBg,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Grant permissions to enable background network firewall",
                color = Dim,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // ── Center: 4 Compact Rows ──
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MinimalPermRow(
                title = "Shizuku Service",
                desc = "ADB policy execution",
                granted = shizukuOk,
                required = true,
                btnText = "Authorize",
                onGrant = onGrantShizuku
            )

            MinimalPermRow(
                title = "Usage Access",
                desc = "Foreground app detection",
                granted = usageOk,
                required = true,
                btnText = "Grant",
                onGrant = onGrantUsage
            )

            MinimalPermRow(
                title = "Notifications",
                desc = "Background service status",
                granted = notifOk,
                required = true,
                btnText = "Grant",
                onGrant = onGrantNotif
            )

            MinimalPermRow(
                title = "Battery Optimization",
                desc = "Keep service alive in background",
                granted = batteryOk,
                required = false,
                btnText = "Ignore",
                onGrant = onGrantBattery
            )
        }

        // ── Bottom: Continue Button ──
        Button(
            onClick = onContinue,
            enabled = allRequired,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                disabledContainerColor = Surf2
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (allRequired) "Continue to App" else "Grant required permissions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (allRequired) Color.White else Dim
            )
        }
    }
}

/* ─── Compact Minimal Permission Row (Single Line Clean Design) ─── */
@Composable
fun MinimalPermRow(
    title: String,
    desc: String,
    granted: Boolean,
    required: Boolean,
    btnText: String,
    onGrant: () -> Unit
) {
    Surface(
        color = Surf,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (granted) Color(0x334CAF50) else Div),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Icon
            Icon(
                if (granted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if (granted) Green else Dim,
                modifier = Modifier.size(20.dp)
            )

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = OnBg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (!granted && required) {
                        Surface(
                            color = Color(0x24EF5350),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "REQUIRED",
                                color = Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(1.dp))
                Text(
                    text = desc,
                    color = Dim,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Grant Action Button (Fixed Width 76.dp & Height 32.dp)
            if (granted) {
                Surface(
                    color = Color(0x1F4CAF50),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0x334CAF50)),
                    modifier = Modifier.width(76.dp).height(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "DONE",
                            color = Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrant,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (required) Color(0x264CAF50) else Color(0x1AFFFFFF),
                        contentColor = if (required) Green else OnBg
                    ),
                    border = BorderStroke(1.dp, if (required) Color(0x554CAF50) else Color(0x2AFFFFFF)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(76.dp).height(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(btnText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/* ─── Home Screen ─── */
@Composable
fun HomeContent(
    apps: List<AppInfo>, showPkg: Boolean, shizukuOk: Boolean, serviceOn: Boolean,
    onShizukuRetry: () -> Unit, onToggleWifi: (String) -> Unit, onToggleData: (String) -> Unit
) {
    var filterMode by remember { mutableStateOf("all") } // all | blocked
    val displayedApps = remember(apps, filterMode) {
        if (filterMode == "blocked") apps.filter { it.wifiBlocked || it.dataBlocked }
        else apps
    }
    val blockedCount = apps.count { it.wifiBlocked || it.dataBlocked }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (!shizukuOk) item {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0x1CEF5350)).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(18.dp))
                Text("Shizuku is not running. Start via Wireless Debugging.", color = Color(0xFFEF9A9A), fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onShizukuRetry) { Text("RETRY", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        } else if (!serviceOn) item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x26FFA726))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PauseCircle, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(17.dp))
                Text(
                    "Firewall is paused. Turn on the top switch to activate.",
                    color = Color(0xFFFFE0B2),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(Color(0xFF1a1a1a))
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Quick Filter Chips
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (filterMode == "all") Color(0x264CAF50) else Color(0xFF262626),
                        border = BorderStroke(1.dp, if (filterMode == "all") Color(0x664CAF50) else Color(0xFF333333)),
                        modifier = Modifier.clickable { filterMode = "all" }
                    ) {
                        Text(
                            text = "All (${apps.size})",
                            color = if (filterMode == "all") Green else Dim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Blocked Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (filterMode == "blocked") Color(0x26EF5350) else Color(0xFF262626),
                        border = BorderStroke(1.dp, if (filterMode == "blocked") Color(0x66EF5350) else Color(0xFF333333)),
                        modifier = Modifier.clickable { filterMode = if (filterMode == "blocked") "all" else "blocked" }
                    ) {
                        Text(
                            text = "Restricted ($blockedCount)",
                            color = if (filterMode == "blocked") Red else Dim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Right: Column Labels
                listOf("WiFi", "Data").forEach { label ->
                    Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (label == "WiFi") Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                                null, tint = Color(0xFF666666), modifier = Modifier.size(13.dp))
                            Text(label, color = Color(0xFF555555), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            HorizontalDivider(color = Div)
        }

        items(displayedApps, key = { it.packageName }) { app ->
            AppRow(app, showPkg, onToggleWifi = { onToggleWifi(app.packageName) }, onToggleData = { onToggleData(app.packageName) })
            HorizontalDivider(color = Color(0xFF252525), modifier = Modifier.padding(start = 74.dp))
        }
    }
}

/* ─── App Row ─── */
@Composable
fun AppRow(app: AppInfo, showPkg: Boolean, onToggleWifi: () -> Unit, onToggleData: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surf2),
            contentAlignment = Alignment.Center
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.appName,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = app.appName.take(1).uppercase(),
                    color = Green,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)) {
            Text(app.appName, color = OnBg, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (showPkg) Text(app.packageName, color = Color(0xFF666666), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        NetBtn(blocked = app.wifiBlocked, onClick = onToggleWifi, isWifi = true)
        NetBtn(blocked = app.dataBlocked, onClick = onToggleData, isWifi = false)
    }
}

/* ─── NetGuard icon button ─── */
@Composable
fun NetBtn(blocked: Boolean, onClick: () -> Unit, isWifi: Boolean) {
    Box(modifier = Modifier.size(52.dp).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(11.dp),
            color = if (blocked) Color(0x2EEF5350) else Color(0x1F4CAF50)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                    null,
                    tint = if (blocked) Red else Green,
                    modifier = Modifier.size(20.dp)
                )
                if (blocked) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(38.dp)) {
                        drawLine(
                            color = Color(0xFFEF5350),
                            start = Offset(size.width * 0.22f, size.height * 0.22f),
                            end   = Offset(size.width * 0.78f, size.height * 0.78f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/* ─── Logs Screen ─── */
@Composable
fun LogsScreen(logs: List<String>) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0e0e0e))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF141414))
                .padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("V", Color(0xFFaaaaaa), "Verbose"),
                Triple("D", Blue, "Debug"),
                Triple("I", Green, "Info"),
                Triple("W", Yellow, "Warn"),
                Triple("E", Red, "Error")
            ).forEach { (t, c, full) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(t, color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(full, color = Color(0xFF777777), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        HorizontalDivider(color = Color(0xFF1e1e1e))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (logs.isEmpty()) item {
                Text("No log entries.", color = Color(0xFF333333), fontSize = 13.sp, modifier = Modifier.padding(top = 48.dp).fillMaxWidth())
            } else items(logs) { log ->
                Text(log, color = when {
                    "  W  " in log -> Color(0xFFFFE082)
                    "  E  " in log -> Color(0xFFFCA5A5)
                    "  D  " in log -> Color(0xFF93C5FD)
                    else           -> Color(0xFFB0BEC5)
                }, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 17.sp)
            }
        }
    }
}

/* ─── Settings Screen ─── */
@Composable
fun SettingsScreen(
    privilege: String, bootStart: Boolean, serviceOn: Boolean,
    showPkg: Boolean, minNotif: Boolean, blockNotifs: Boolean, screenOffShield: Boolean,
    shizukuOk: Boolean, usageOk: Boolean, notifOk: Boolean, batteryOk: Boolean,
    onPrivilege: (String) -> Unit, onBoot: (Boolean) -> Unit, onService: (Boolean) -> Unit,
    onPkg: (Boolean) -> Unit, onNotif: (Boolean) -> Unit,
    onBlockNotifs: (Boolean) -> Unit,
    onScreenOffShield: (Boolean) -> Unit,
    onGrantUsage: () -> Unit, onGrantNotif: () -> Unit, onGrantBattery: () -> Unit, onGrantShizuku: () -> Unit
) {
    val ctx = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().background(BG)) {

        // Privilege Provider
        item { SecHeader("Privilege Provider") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                listOf(
                    Triple("shizuku", "Shizuku",   "ADB Wireless Debugging — Recommended"),
                    Triple("dhizuku", "Dhizuku",   "Device Owner — Survives reboot"),
                    Triple("root",    "Root (su)", "Superuser binary — Full root access")
                ).forEachIndexed { i, (id, label, sub) ->
                    PrefRow(label, sub, onClick = { onPrivilege(id) }, trailing = {
                        RadioButton(selected = privilege == id, onClick = { onPrivilege(id) },
                            colors = RadioButtonDefaults.colors(selectedColor = Green, unselectedColor = Dim))
                    })
                    if (i < 2) HorizontalDivider(color = Div)
                }
            }
        }

        // Permissions
        item { SecHeader("Permissions") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                listOf(
                    Triple("Shizuku Authorization", shizukuOk, onGrantShizuku),
                    Triple("Usage Access",          usageOk,   onGrantUsage),
                    Triple("Post Notifications",    notifOk,   onGrantNotif),
                    Triple("Battery Optimization",  batteryOk, onGrantBattery)
                ).forEachIndexed { i, (label, granted, onGrant) ->
                    PrefRow(label, if (granted) "✓ Granted" else "Not granted", trailing = {
                        if (granted)
                            Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(20.dp))
                        else
                            OutlinedButton(onClick = onGrant, shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Dim),
                                border = BorderStroke(1.dp, Color(0xFF444444)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) { Text("Grant", fontSize = 12.sp) }
                    })
                    if (i < 3) HorizontalDivider(color = Div)
                }
            }
        }

        // Service
        item { SecHeader("Service") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow("Enabled", "Master on/off switch for the firewall",
                    trailing = { NetCordonSwitch(checked = serviceOn, onCheckedChange = onService) })
                HorizontalDivider(color = Div)
                PrefRow("Start on boot", "Automatically start after device reboot",
                    trailing = { NetCordonSwitch(checked = bootStart, onCheckedChange = onBoot) })
            }
        }

        // Protection Rules
        item { SecHeader("Protection Rules") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow("Block when screen is off", "Auto-cut background traffic & mute notifications when device is locked",
                    trailing = { NetCordonSwitch(checked = screenOffShield, onCheckedChange = onScreenOffShield) })
                HorizontalDivider(color = Div)
                PrefRow("Block app notifications", "Auto-silence popups, alerts & vibrations when app is restricted",
                    trailing = { NetCordonSwitch(checked = blockNotifs, onCheckedChange = onBlockNotifs) })
            }
        }

        // Display
        item { SecHeader("Display") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow("Show package names", "Display package name below each app",
                    trailing = { NetCordonSwitch(checked = showPkg, onCheckedChange = onPkg) })
            }
        }

        // Notification
        item { SecHeader("Notification") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow("Minimal notification", "Show a compact status bar icon only",
                    trailing = { NetCordonSwitch(checked = minNotif, onCheckedChange = onNotif) })
            }
        }

        // Developer & Support
        item { SecHeader("Developer & Support") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow(
                    label = "Developer Email",
                    sublabel = "sachinmandawi@gmail.com · Tap to open Gmail",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:sachinmandawi@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "NetCordon Support / Feedback")
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Email, null, tint = Green, modifier = Modifier.size(18.dp))
                    }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "GitHub Profile",
                    sublabel = "github.com/sachinmandawi",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sachinmandawi"))
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Language, null, tint = Green, modifier = Modifier.size(18.dp))
                    }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "GitHub Repository & Releases",
                    sublabel = "github.com/sachinmandawi/NetCordon",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sachinmandawi/NetCordon"))
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Language, null, tint = Green, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        // About
        item { SecHeader("About") }
        item {
            Column(modifier = Modifier.background(Surf)) {
                PrefRow("NetCordon", "Version 1.0 · Shizuku-powered user app firewall")
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

/* ─── Settings Section Header ─── */
@Composable
fun SecHeader(label: String) {
    Text(label, color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp, modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp))
}

/* ─── Settings Preference Row ─── */
@Composable
fun PrefRow(label: String, sublabel: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = OnBg, fontSize = 15.sp, lineHeight = 20.sp)
            if (sublabel != null)
                Text(sublabel, color = Dim, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
        }
        trailing?.invoke()
    }
}
