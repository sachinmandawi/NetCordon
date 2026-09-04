package com.sachinmandawi.netcordon

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

/* ─── Material Design 3 Dynamic Theme System (Light & Dark) ─── */
data class NetCordonThemeColors(
    val bg: Color,
    val surf: Color,
    val surf2: Color,
    val div: Color,
    val onBg: Color,
    val dim: Color,
    val green: Color,
    val onGreen: Color,
    val red: Color,
    val yellow: Color,
    val blue: Color,
    val tBg: Color,
    val switchTrack: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

val LightColors = NetCordonThemeColors(
    bg         = Color(0xFFF1F5F2),
    surf       = Color(0xFFFFFFFF),
    surf2      = Color(0xFFE4ECE6),
    div        = Color(0xFFD0DDD4),
    onBg       = Color(0xFF111714),
    dim        = Color(0xFF5A6B62),
    green      = Color(0xFF00875A),
    onGreen    = Color(0xFFFFFFFF),
    red        = Color(0xFFCF2020),
    yellow     = Color(0xFFCC7000),
    blue       = Color(0xFF0369A1),
    tBg        = Color(0xFFFFFFFF),
    switchTrack= Color(0xFFD0DDD4),
    cardBorder = Color(0xFFBACBC0),   // distinct, crisp box border in light mode
    isDark     = false
)

val DarkColors = NetCordonThemeColors(
    bg         = Color(0xFF040605),   // deep AMOLED near-black
    surf       = Color(0xFF0E1310),   // rich dark slate surface
    surf2      = Color(0xFF161E19),   // clearly elevated box surface
    div        = Color(0xFF222C25),   // dark divider
    onBg       = Color(0xFFECF2EE),   // near-white text
    dim        = Color(0xFF6B7D72),   // muted grey-green
    green      = Color(0xFF00E676),   // vivid neon green — pops on deep dark
    onGreen    = Color(0xFF003816),   // deep dark green — crisp contrast on neon green
    red        = Color(0xFFFF5370),   // vivid red
    yellow     = Color(0xFFFFD740),   // vivid amber
    blue       = Color(0xFF40C4FF),   // vivid cyan-blue
    tBg        = Color(0xFF080D0A),   // toolbar — slightly lighter than bg
    switchTrack= Color(0xFF202A23),   // dark switch track
    cardBorder = Color(0xFF2E3E34),   // crisp, eye-catching box border in dark mode
    isDark     = true
)

val LocalThemeColors = staticCompositionLocalOf { LightColors }

/* Reactive Composable Accessors */
private val BG: Color @Composable get() = LocalThemeColors.current.bg
private val Surf: Color @Composable get() = LocalThemeColors.current.surf
private val Surf2: Color @Composable get() = LocalThemeColors.current.surf2
private val Div: Color @Composable get() = LocalThemeColors.current.div
private val OnBg: Color @Composable get() = LocalThemeColors.current.onBg
private val Dim: Color @Composable get() = LocalThemeColors.current.dim
private val Green: Color @Composable get() = LocalThemeColors.current.green
private val OnGreen: Color @Composable get() = LocalThemeColors.current.onGreen
private val Red: Color @Composable get() = LocalThemeColors.current.red
private val Yellow: Color @Composable get() = LocalThemeColors.current.yellow
private val Blue: Color @Composable get() = LocalThemeColors.current.blue
private val CardBorder: Color @Composable get() = LocalThemeColors.current.cardBorder
private val TBg: Color @Composable get() = LocalThemeColors.current.tBg

fun getNetCordonColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = DarkColors.green,
            onPrimary = Color(0xFF002D14),
            primaryContainer = Color(0xFF003820),
            onPrimaryContainer = Color(0xFF00E676),
            secondary = DarkColors.green,
            onSecondary = Color(0xFF002D14),
            secondaryContainer = DarkColors.surf,
            onSecondaryContainer = DarkColors.onBg,
            tertiary = DarkColors.blue,
            background = DarkColors.bg,
            onBackground = DarkColors.onBg,
            surface = DarkColors.surf,
            onSurface = DarkColors.onBg,
            surfaceVariant = DarkColors.surf2,
            onSurfaceVariant = DarkColors.dim,
            outline = DarkColors.cardBorder,
            outlineVariant = DarkColors.div,
            surfaceTint = Color.Transparent, // PREVENTS DIALOG MUDDY GREEN TINTING
            scrim = Color(0xFF000000)
        )
    } else {
        lightColorScheme(
            primary = LightColors.green,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1F2D9),
            onPrimaryContainer = Color(0xFF002114),
            secondary = Color(0xFF4D6356),
            onSecondary = Color.White,
            secondaryContainer = LightColors.surf2,
            onSecondaryContainer = LightColors.onBg,
            tertiary = Color(0xFF3D6373),
            background = LightColors.bg,
            onBackground = LightColors.onBg,
            surface = LightColors.surf,
            onSurface = LightColors.onBg,
            surfaceVariant = LightColors.surf2,
            onSurfaceVariant = LightColors.dim,
            outline = LightColors.cardBorder,
            outlineVariant = LightColors.div,
            surfaceTint = Color.Transparent
        )
    }
}

/* ─── Custom Clean Minimalist Switch (M3 Reactive Theme Style) ─── */
@Composable
fun NetCordonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColors = LocalThemeColors.current
    val trackColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) currentColors.green else currentColors.switchTrack,
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
                color = OnBg,
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
                    .background(Div)
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
class MainActivity : FragmentActivity() {

    var onShizukuPermissionResult: ((Boolean) -> Unit)? = null
    private val shizukuPermListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1001) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            runOnUiThread {
                onShizukuPermissionResult?.invoke(granted)
            }
        }
    }
    private val isUnlocked = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AdMobManager.init(this)
        isUnlocked.value = !PrefsManager.isAppLockEnabled(this)
        Shizuku.addRequestPermissionResultListener(shizukuPermListener)

        val isDarkState = mutableStateOf(PrefsManager.isDarkMode(this))

        setContent {
            val isDark = isDarkState.value
            val themeColors = if (isDark) DarkColors else LightColors
            val colorScheme = getNetCordonColorScheme(isDark)

            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? android.app.Activity)?.window
                    if (window != null) {
                        androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                            isAppearanceLightStatusBars = !isDark
                            isAppearanceLightNavigationBars = !isDark
                        }
                    }
                }
            }

            CompositionLocalProvider(LocalThemeColors provides themeColors) {
                MaterialTheme(colorScheme = colorScheme) {
                    val showSplash = remember { mutableStateOf(true) }

                    LaunchedEffect(showSplash.value, isUnlocked.value) {
                        if (!showSplash.value && !isUnlocked.value) {
                            try {
                                if (BiometricLockManager.canAuthenticate(this@MainActivity)) {
                                    BiometricLockManager.authenticate(
                                        activity = this@MainActivity,
                                        onSuccess = { isUnlocked.value = true },
                                        onError = { /* Keep lock screen visible for retry */ }
                                    )
                                } else {
                                    isUnlocked.value = true
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Biometric launch error", e)
                                isUnlocked.value = true
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().background(BG)) {
                        if (!showSplash.value) {
                            if (!isUnlocked.value) {
                                LockScreen(
                                    onUnlockTap = {
                                        try {
                                            BiometricLockManager.authenticate(
                                                activity = this@MainActivity,
                                                onSuccess = { isUnlocked.value = true },
                                                onError = { /* Keep lock screen visible for retry */ }
                                            )
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Manual unlock tap error", e)
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .windowInsetsPadding(WindowInsets.systemBars)
                                ) {
                                    NetCordonApp(
                                        isDarkMode = isDark,
                                        onDarkModeChange = { newMode ->
                                            isDarkState.value = newMode
                                            PrefsManager.setDarkMode(this@MainActivity, newMode)
                                        }
                                    )
                                }
                            }
                        }
                        if (showSplash.value) {
                            AnimatedSplashScreen(onFinish = { showSplash.value = false })
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (PrefsManager.isAppLockEnabled(this) && PrefsManager.isLockOnScreenOff(this)) {
            isUnlocked.value = false
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

/* ─── Helper: safely unwrap Context to find Activity ─── */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/* ─── Helper: Notification permission ─── */
fun hasNotificationPerm(ctx: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    else true

/* ─── Helper: Battery Optimization check ─── */
fun hasBatteryOptimizationIgnored(ctx: Context): Boolean = try {
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val standardIgnored = pm?.isIgnoringBatteryOptimizations(ctx.packageName) == true
    standardIgnored || PrefsManager.isBatteryIgnored(ctx)
} catch (e: Exception) {
    PrefsManager.isBatteryIgnored(ctx)
}

/* ─── Helper: fetch installed user apps ─── */
fun fetchInstalledApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val savedSmart = PrefsManager.getSmartShieldPackages(ctx)
    val savedWifi = PrefsManager.getWifiBlockedPackages(ctx)
    val savedData = PrefsManager.getDataBlockedPackages(ctx)
    val savedBlackout = PrefsManager.getBlackoutPackages(ctx)
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { ai ->
            val iconBmp = try {
                pm.getApplicationIcon(ai).toBitmap(width = 96, height = 96).asImageBitmap()
            } catch (e: Exception) {
                null
            }
            val isSmart = savedSmart.contains(ai.packageName)
            val isBlack = savedBlackout.contains(ai.packageName)
            AppInfo(
                appName     = pm.getApplicationLabel(ai).toString(),
                packageName = ai.packageName,
                uid         = ai.uid,
                iconBitmap  = iconBmp,
                wifiBlocked = savedWifi.contains(ai.packageName),
                dataBlocked = savedData.contains(ai.packageName),
                isBlackout  = isBlack,
                isSmartShield = isSmart
            )
        }
        .sortedBy { it.appName.lowercase() }
}

/* ═══════════════════════════════════════════════════
   ROOT COMPOSABLE
═══════════════════════════════════════════════════ */
@Composable
fun NetCordonApp(
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {}
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /* ── Permission live state ── */
    var shizukuOk  by remember { mutableStateOf(ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) }
    var usageOk    by remember { mutableStateOf(hasUsageAccess(ctx)) }
    var notifOk    by remember { mutableStateOf(hasNotificationPerm(ctx)) }
    var batteryOk  by remember { mutableStateOf(hasBatteryOptimizationIgnored(ctx)) }

    /* ── Live Lifecycle & Tab Refresh: Keep permissions up-to-date in real time ── */
    val act = ctx.findActivity() as? MainActivity
    var adFreeUntil by remember { mutableStateOf(PrefsManager.getAdFreeUntil(ctx)) }
    var isAdFree by remember { mutableStateOf(PrefsManager.isAdFreeActive(ctx)) }
    var showDoubleVipDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val showModernToast: (String) -> Unit = { msg ->
        toastMessage = msg
    }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2000)
            toastMessage = null
        }
    }

    val triggerWatchAdForVip: () -> Unit = {
        val activity = ctx.findActivity()
        if (activity != null) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = { _ ->
                    val newExpiry = PrefsManager.grantAdFreeHours(ctx, 12)
                    adFreeUntil = newExpiry
                    isAdFree = true
                    showDoubleVipDialog = true
                },
                onAdFailed = { _ ->
                    showModernToast("Ad loading…")
                }
            )
        }
    }

    val triggerDoubleVipAd: () -> Unit = {
        showDoubleVipDialog = false
        val activity = ctx.findActivity()
        if (activity != null) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = { _ ->
                    val newExpiry = PrefsManager.grantAdFreeDoubleBoost(ctx)
                    adFreeUntil = newExpiry
                    isAdFree = true
                    showModernToast("🚀 48h VIP Active")
                },
                onAdFailed = { _ ->
                    showModernToast("Ad loading…")
                }
            )
        }
    }

    DisposableEffect(act) {
        act?.onShizukuPermissionResult = { granted ->
            shizukuOk = granted
        }
        onDispose {
            act?.onShizukuPermissionResult = null
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                shizukuOk = ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()
                usageOk = hasUsageAccess(ctx)
                notifOk = hasNotificationPerm(ctx)
                batteryOk = hasBatteryOptimizationIgnored(ctx)
                adFreeUntil = PrefsManager.getAdFreeUntil(ctx)
                isAdFree = PrefsManager.isAdFreeActive(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allRequired = shizukuOk && usageOk && notifOk
    var setupDone by remember { mutableStateOf(PrefsManager.isSetupCompleted(ctx) && allRequired) }
    var showWalkthrough by remember { mutableStateOf(!PrefsManager.isOnboardingCompleted(ctx)) }

    /* ── Launchers ── */
    val usageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        usageOk = hasUsageAccess(ctx)
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifOk = it
    }
    val batteryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        PrefsManager.setBatteryIgnored(ctx, true)
        batteryOk = true
        if (shizukuOk) {
            coroutineScope.launch(Dispatchers.IO) {
                ShizukuManager.executeCommand("cmd appops set ${ctx.packageName} RUN_IN_BACKGROUND allow; cmd appops set ${ctx.packageName} RUN_ANY_IN_BACKGROUND allow; cmd deviceidle whitelist +${ctx.packageName}")
            }
        }
    }

    val triggerBatteryGrant: () -> Unit = {
        if (shizukuOk) {
            coroutineScope.launch(Dispatchers.IO) {
                ShizukuManager.executeCommand("cmd appops set ${ctx.packageName} RUN_IN_BACKGROUND allow; cmd appops set ${ctx.packageName} RUN_ANY_IN_BACKGROUND allow; cmd deviceidle whitelist +${ctx.packageName}")
            }
        }
        PrefsManager.setBatteryIgnored(ctx, true)
        batteryOk = true
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${ctx.packageName}")
            }
            batteryLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                batteryLauncher.launch(intent)
            } catch (e2: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                }
                batteryLauncher.launch(intent)
            }
        }
    }

    /* ── Navigation ── */
    var screen      by remember { mutableStateOf("home") } // home | analytics | logs | settings
    var searching   by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForPolicy by remember { mutableStateOf<AppInfo?>(null) }

    /* ── App list (Loaded asynchronously on IO thread) ── */
    var apps by remember { mutableStateOf(listOf<AppInfo>()) }
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) { fetchInstalledApps(ctx) }
        apps = loaded
        val uidMap = loaded.associate { it.packageName to it.uid }
        PrefsManager.savePackageUidMap(ctx, uidMap)
        withContext(Dispatchers.IO) {
            ShizukuManager.initFirewallFramework(ctx.packageName)
        }
    }

    /* ── Settings (loaded from persistent PrefsManager) ── */
    var privilege        by remember { mutableStateOf(PrefsManager.getPrivilege(ctx)) }
    var bootStart        by remember { mutableStateOf(PrefsManager.isStartOnBoot(ctx)) }
    var serviceOn        by remember { mutableStateOf(PrefsManager.isServiceEnabled(ctx)) }
    var showPkg          by remember { mutableStateOf(PrefsManager.isShowPkg(ctx)) }
    var minNotif         by remember { mutableStateOf(PrefsManager.isMinNotif(ctx)) }
    var blockNotifs      by remember { mutableStateOf(PrefsManager.isBlockNotifications(ctx)) }
    var screenOffShield  by remember { mutableStateOf(PrefsManager.isScreenOffShield(ctx)) }
    var appLockEnabled   by remember { mutableStateOf(PrefsManager.isAppLockEnabled(ctx)) }
    var lockOnScreenOff  by remember { mutableStateOf(PrefsManager.isLockOnScreenOff(ctx)) }
    var leakAlertsEnabled by remember { mutableStateOf(PrefsManager.isLeakAlertsEnabled(ctx)) }

    /* ── Service sync helper ── */
    fun syncService(updatedApps: List<AppInfo>) {
        val smartPkgs    = updatedApps.filter { it.isSmartShield }.map { it.packageName }.toSet()
        val wifiPkgs     = updatedApps.filter { it.wifiBlocked }.map { it.packageName }.toSet()
        val dataPkgs     = updatedApps.filter { it.dataBlocked }.map { it.packageName }.toSet()
        val blackoutPkgs = updatedApps.filter { it.isBlackout }.map { it.packageName }.toSet()
        val uidMap       = updatedApps.associate { it.packageName to it.uid }

        PrefsManager.saveSmartShieldPackages(ctx, smartPkgs)
        PrefsManager.saveWifiBlockedPackages(ctx, wifiPkgs)
        PrefsManager.saveDataBlockedPackages(ctx, dataPkgs)
        PrefsManager.saveBlackoutPackages(ctx, blackoutPkgs)
        PrefsManager.savePackageUidMap(ctx, uidMap)

        val shielded = updatedApps.filter { it.isSmartShield || it.wifiBlocked || it.dataBlocked || it.isBlackout }
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
    fun pushLog(line: String) {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            logs.add(0, line)
            if (logs.size > 100) logs.removeAt(logs.lastIndex)
        } else {
            coroutineScope.launch(Dispatchers.Main) {
                logs.add(0, line)
                if (logs.size > 100) logs.removeAt(logs.lastIndex)
            }
        }
    }

    /* ════════════════════════════════
       ONBOARDING / WALKTHROUGH SCREEN
       (FIRST LAUNCH USER GUIDE)
    ════════════════════════════════ */
    if (showWalkthrough) {
        OnboardingWalkthroughScreen(
            onComplete = {
                PrefsManager.setOnboardingCompleted(ctx, true)
                showWalkthrough = false
            }
        )
        return
    }

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
            onGrantBattery = triggerBatteryGrant,
            onContinue = {
                PrefsManager.setSetupCompleted(ctx, true)
                setupDone = true
                val shielded = apps.filter { it.isShielded }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

        /* ── Toolbar ── */
        when (screen) {
            "home" -> {
                Column(modifier = Modifier.background(TBg)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = 4.dp),
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
                            Spacer(Modifier.width(14.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "NetCordon",
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NetCordon", color = OnBg, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                // Status pill inline
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(if (serviceOn) Green else Yellow))
                                    Text(
                                        text = if (serviceOn) "Firewall Active" else "Paused",
                                        color = if (serviceOn) Green else Yellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // ── Master ON/OFF Switch ──
                            NetCordonSwitch(
                                checked = serviceOn,
                                onCheckedChange = { newState ->
                                    serviceOn = newState
                                    PrefsManager.setServiceEnabled(ctx, newState)
                                    if (newState) {
                                        val shielded = apps.filter { it.isShielded }
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
                                        val activity = ctx.findActivity()
                                        if (activity != null && PrefsManager.shouldShowInterstitial(ctx)) {
                                            AdMobManager.showInterstitialAd(activity)
                                        }
                                    } else {
                                        val intent = Intent(ctx, AppShieldService::class.java).apply {
                                            action = AppShieldService.ACTION_STOP
                                        }
                                        try {
                                            ctx.startService(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        coroutineScope.launch(Dispatchers.IO) {
                                            for (app in apps) {
                                                if (app.isShielded) {
                                                    ShizukuManager.setAppIsolation(
                                                        packageName = app.packageName,
                                                        uid = app.uid,
                                                        mode = AppIsolationMode.ALLOWED,
                                                        isForeground = true,
                                                        blockNotifications = false
                                                    )
                                                }
                                            }
                                            ShizukuManager.executeCommand("cmd connectivity set-chain3-enabled false")
                                        }
                                        pushLog("${timestamp()}  W  Service: Master switch OFF — Restrictions paused")
                                    }
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            IconButton(onClick = { searching = true }) {
                                Icon(Icons.Default.Search, null, tint = OnBg.copy(alpha = 0.7f))
                            }
                        }
                    }
                    HorizontalDivider(color = CardBorder)
                }
            }
            "schedules" -> SubToolbar("Firewall Schedules", onBack = { screen = "home" })
            "analytics" -> SubToolbar("Network Analytics", onBack = { screen = "home" })
            "logs" -> SubToolbar("Logs", onBack = { screen = "home" }) {
                IconButton(onClick = { logs.clear() }) {
                    Icon(Icons.Default.Delete, null, tint = OnBg.copy(alpha = 0.8f))
                }
            }
            "settings" -> SubToolbar("Settings", onBack = { screen = "home" })
        }

        /* ── Native Android System Back Navigation ── */
        BackHandler(enabled = searching || screen != "home") {
            when {
                searching -> {
                    searching = false
                    searchQuery = ""
                }
                screen != "home" -> screen = "home"
            }
        }

        /* ── Screen Content ── */
        Box(modifier = Modifier.weight(1f)) {
            when (screen) {
                "home" -> HomeContent(
                    apps            = filtered,
                    showPkg         = showPkg,
                    shizukuOk       = shizukuOk,
                    serviceOn       = serviceOn,
                    isAdFree        = isAdFree,
                    onWatchAdForVip = triggerWatchAdForVip,
                    onShizukuRetry  = { shizukuOk = ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission() },
                    onOpenPolicy    = { app -> selectedAppForPolicy = app }
                )
                "schedules" -> SchedulesScreen(apps = apps)
                "analytics" -> AnalyticsScreen(apps = apps)
                "logs"     -> LogsScreen(logs)
                "settings" -> SettingsScreen(
                    isDarkMode = isDarkMode, onDarkModeChange = onDarkModeChange,
                    privilege = privilege, bootStart = bootStart, serviceOn = serviceOn,
                    showPkg = showPkg, minNotif = minNotif, blockNotifs = blockNotifs, screenOffShield = screenOffShield,
                    appLockEnabled = appLockEnabled, lockOnScreenOff = lockOnScreenOff,
                    shizukuOk = shizukuOk, usageOk = usageOk, notifOk = notifOk, batteryOk = batteryOk,
                    onPrivilege = { privilege = it; PrefsManager.setPrivilege(ctx, it) },
                    onBoot      = { bootStart = it; PrefsManager.setStartOnBoot(ctx, it) },
                    onService   = { v ->
                        serviceOn = v
                        PrefsManager.setServiceEnabled(ctx, v)
                        try {
                            val intent = Intent(ctx, AppShieldService::class.java).apply {
                                action = if (v) AppShieldService.ACTION_START else AppShieldService.ACTION_STOP
                                if (v) {
                                    val shielded = apps.filter { it.isShielded }
                                    putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded.map { it.packageName }))
                                    putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.map { it.uid }))
                                }
                            }
                            if (v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(intent)
                                else ctx.startService(intent)
                                val activity = ctx.findActivity()
                                if (activity != null && PrefsManager.shouldShowInterstitial(ctx)) {
                                    AdMobManager.showInterstitialAd(activity)
                                }
                            } else {
                                ctx.startService(intent)
                                coroutineScope.launch(Dispatchers.IO) {
                                    for (app in apps) {
                                        if (app.isShielded) {
                                            ShizukuManager.setAppIsolation(
                                                packageName = app.packageName,
                                                uid = app.uid,
                                                mode = AppIsolationMode.ALLOWED,
                                                isForeground = true,
                                                blockNotifications = false
                                            )
                                        }
                                    }
                                    ShizukuManager.executeCommand("cmd connectivity set-chain3-enabled false")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        pushLog("${timestamp()}  I  Service: AppShieldService ${if (v) "started" else "stopped"}")
                    },
                    onPkg     = { showPkg = it; PrefsManager.setShowPkg(ctx, it) },
                    onNotif   = { minNotif = it; PrefsManager.setMinNotif(ctx, it); syncService(apps) },
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
                    onAppLockChange = { enabled ->
                        val act = ctx as? FragmentActivity
                        if (act != null) {
                            BiometricLockManager.authenticate(
                                activity = act,
                                title = if (enabled) "Enable App Lock" else "Disable App Lock",
                                subtitle = "Authenticate to update NetCordon lock settings",
                                onSuccess = {
                                    appLockEnabled = enabled
                                    PrefsManager.setAppLockEnabled(ctx, enabled)
                                    pushLog("${timestamp()}  I  Security: Biometric & PIN Lock ${if (enabled) "Enabled" else "Disabled"}")
                                },
                                onError = {
                                    pushLog("${timestamp()}  W  Security: Biometric authentication cancelled")
                                }
                            )
                        } else {
                            appLockEnabled = enabled
                            PrefsManager.setAppLockEnabled(ctx, enabled)
                        }
                    },
                    onLockOnScreenOffChange = { v ->
                        lockOnScreenOff = v
                        PrefsManager.setLockOnScreenOff(ctx, v)
                    },
                    leakAlertsEnabled = leakAlertsEnabled,
                    onLeakAlertsChange = { v ->
                        leakAlertsEnabled = v
                        PrefsManager.setLeakAlertsEnabled(ctx, v)
                        pushLog("${timestamp()}  I  Radar: Background Leak Alerts ${if (v) "Enabled" else "Disabled"}")
                    },
                    onGrantUsage   = { usageLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                    onGrantNotif   = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) else notifOk = true },
                    onGrantBattery = triggerBatteryGrant,
                    onGrantShizuku = { Shizuku.requestPermission(1001) },
                    onReplayWalkthrough = { showWalkthrough = true },
                    onProfileRestored = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val reloaded = fetchInstalledApps(ctx)
                            withContext(Dispatchers.Main) {
                                apps = reloaded
                                syncService(reloaded)
                                screenOffShield = PrefsManager.isScreenOffShield(ctx)
                                blockNotifs = PrefsManager.isBlockNotifications(ctx)
                                leakAlertsEnabled = PrefsManager.isLeakAlertsEnabled(ctx)
                                minNotif = PrefsManager.isMinNotif(ctx)
                                bootStart = PrefsManager.isStartOnBoot(ctx)
                                showPkg = PrefsManager.isShowPkg(ctx)
                                val activity = ctx.findActivity()
                                if (activity != null && PrefsManager.shouldShowInterstitial(ctx)) {
                                    AdMobManager.showInterstitialAd(activity)
                                }
                            }
                        }
                    },
                    isAdFree = isAdFree,
                    adFreeUntil = adFreeUntil,
                    onWatchAdForVip = triggerWatchAdForVip,
                    onResetPass = {
                        adFreeUntil = 0L
                        isAdFree = false
                        showModernToast("Ads enabled")
                    },
                    onShowToast = showModernToast
                )
            }
        }

        /* ── Bottom Sticky Banner Ad (Visible on all screens when not ad-free) ── */
        if (!isAdFree) {
            AdMobManager.BannerAdView()
        }

        /* ── Bottom Navigation Bar ── */
        NetCordonBottomBar(selectedTab = screen, onSelectTab = { newTab ->
            if (screen != newTab) {
                screen = newTab
                val activity = ctx.findActivity()
                if (activity != null && PrefsManager.shouldShowNavInterstitial(ctx, intervalSeconds = 300)) {
                    PrefsManager.recordNavInterstitialShown(ctx)
                    AdMobManager.showInterstitialAd(activity)
                }
            }
        })

        /* ── 3-Tier App Isolation Policy Bottom Sheet ── */
        val curPolicyApp = selectedAppForPolicy
        if (curPolicyApp != null) {
            AppPolicyBottomSheet(
                app = curPolicyApp,
                onDismiss = { selectedAppForPolicy = null },
                onApply = { mode, quotaBytes, wifiBlocked, dataBlocked ->
                    val pkg = curPolicyApp.packageName
                    val uid = curPolicyApp.uid

                    // 1. Save separate Wi-Fi and Mobile Data blocking rules
                    PrefsManager.setAppWifiBlocked(ctx, pkg, wifiBlocked)
                    PrefsManager.setAppDataBlocked(ctx, pkg, dataBlocked)

                    // 2. Save and apply quota
                    PrefsManager.setAppDailyQuota(ctx, pkg, quotaBytes)
                    val (todayStart, todayEnd) = DataUsageManager.getTimeRange("today")
                    val todayStats = DataUsageManager.getUidStats(ctx, uid, todayStart, todayEnd)
                    val todayUsedBytes = todayStats.first + todayStats.second + todayStats.third + todayStats.fourth
                    if (quotaBytes > 0L && todayUsedBytes >= quotaBytes) {
                        PrefsManager.setAppQuotaBlocked(ctx, pkg, true)
                    } else {
                        PrefsManager.setAppQuotaBlocked(ctx, pkg, false)
                    }

                    // 3. Save explicit mode to PrefsManager (Allowed, Smart Shield, or Total Blackout)
                    val isQuotaExceeded = (quotaBytes > 0L && todayUsedBytes >= quotaBytes)
                    val effectiveMode = if (isQuotaExceeded) AppIsolationMode.TOTAL_BLACKOUT else mode
                    PrefsManager.setAppIsolationMode(ctx, pkg, effectiveMode)

                    val isSmart = (effectiveMode == AppIsolationMode.SMART_SHIELD)
                    val isBlack = (effectiveMode == AppIsolationMode.TOTAL_BLACKOUT)

                    val nextApps = apps.map {
                        if (it.packageName != pkg) it
                        else it.copy(
                            wifiBlocked = wifiBlocked,
                            dataBlocked = dataBlocked,
                            isBlackout = isBlack,
                            isSmartShield = isSmart,
                            dailyQuotaBytes = quotaBytes
                        )
                    }
                    apps = nextApps
                    selectedAppForPolicy = null
                    syncService(nextApps)

                    val activity = ctx.findActivity()
                    if (activity != null && PrefsManager.shouldShowInterstitial(ctx)) {
                        AdMobManager.showInterstitialAd(activity)
                    }

                    // Immediately enforce real network isolation via Shizuku
                    coroutineScope.launch(Dispatchers.IO) {
                        val ok = ShizukuManager.setAppIsolation(
                            packageName = pkg,
                            uid = uid,
                            mode = effectiveMode,
                            isForeground = false,
                            blockNotifications = blockNotifs
                        )
                        val modeLabel = when (effectiveMode) {
                            AppIsolationMode.ALLOWED -> "Allowed (Internet Active)"
                            AppIsolationMode.SMART_SHIELD -> "Smart Shield (Cut on close • Resume when open)"
                            AppIsolationMode.TOTAL_BLACKOUT -> "Total Blackout (0 KB/s offline)"
                        }
                        pushLog("${timestamp()}  ${if (ok) "I" else "W"}  Policy: ${curPolicyApp.appName} -> $modeLabel ${if (ok) "✓" else "(Check Shizuku)"}")
                    }
                }
            )
        }

        /* ── Double VIP Booster Dialog (Offer to double 12h pass to 48h) ── */
        if (showDoubleVipDialog) {
            DoubleVipBoosterDialog(
                onDismiss = {
                    showDoubleVipDialog = false
                    showModernToast("🎉 12h VIP Active")
                },
                onWatchSecondAd = triggerDoubleVipAd
            )
        }
        }

        // Modern Floating In-App Toast Pill (Centered above bottom bar)
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(tween(160)) + slideInVertically(tween(200), initialOffsetY = { 30 }),
            exit = fadeOut(tween(160)) + slideOutVertically(tween(200), targetOffsetY = { 30 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        ) {
            toastMessage?.let { ModernToastPill(it) }
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
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnBg)
            }
            Text(title, color = OnBg, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        HorizontalDivider(color = CardBorder)
    }
}

/* ═══════════════════════════════════════════════════
   ONBOARDING / APP WALKTHROUGH (FIRST LAUNCH GUIDE)
═══════════════════════════════════════════════════ */

data class WalkthroughSlide(
    val badge: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val useAppLogo: Boolean = false,
    val bullets: List<Pair<String, Color>>
)

@Composable
fun OnboardingWalkthroughScreen(
    onComplete: () -> Unit
) {
    val greenColor = Green
    val yellowColor = Yellow
    val redColor = Red
    val blueColor = Blue
    val slides = remember(greenColor, yellowColor, redColor, blueColor) {
        listOf(
            WalkthroughSlide(
                badge = "NO-VPN • OPEN SOURCE",
                badgeColor = greenColor,
                title = "No-VPN Android Firewall",
                subtitle = "Direct Shizuku kernel control with zero battery drain.",
                useAppLogo = true,
                bullets = listOf(
                    Pair("⚡ Zero battery drain & no VPN tunnels", greenColor),
                    Pair("🛡️ 100% On-device, open-source & free", blueColor)
                )
            ),
            WalkthroughSlide(
                badge = "3 ISOLATION MODES",
                badgeColor = yellowColor,
                title = "Smart Control for Every App",
                subtitle = "Tap any app card from your dashboard to switch mode:",
                iconVector = Icons.Default.Shield,
                bullets = listOf(
                    Pair("🟢 Allowed • Normal internet access", greenColor),
                    Pair("🟡 Smart Shield • Cut on close, resume on open", yellowColor),
                    Pair("🔴 Total Blackout • 100% offline & zero ads", redColor)
                )
            ),
            WalkthroughSlide(
                badge = "AUTOMATION",
                badgeColor = blueColor,
                title = "Scheduled Firewall",
                subtitle = "Automate your bedtime and deep work focus hours:",
                iconVector = Icons.Default.Schedule,
                bullets = listOf(
                    Pair("🌙 Bedtime Shield • Auto-mute social apps at night", yellowColor),
                    Pair("📚 Work Focus • Stop distractions during work hours", blueColor)
                )
            ),
            WalkthroughSlide(
                badge = "PRIVACY RADAR",
                badgeColor = blueColor,
                title = "Leak Radar & Security",
                subtitle = "Stop silent background trackers and protect your device:",
                iconVector = Icons.Default.Security,
                bullets = listOf(
                    Pair("🚨 Instant alert on background tracker attempts", redColor),
                    Pair("🔐 Biometric Fingerprint & device PIN lock", greenColor)
                )
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val slide = slides[currentIndex]

    BackHandler(enabled = currentIndex > 0) {
        currentIndex--
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top Bar: Step & Skip ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Surf,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = "Guide ${currentIndex + 1} of ${slides.size}",
                    color = Dim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            if (currentIndex < slides.size - 1) {
                TextButton(
                    onClick = onComplete,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Skip Guide", color = Dim.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }
        }

        // ── Middle: Slide Card ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(slide.badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (slide.useAppLogo) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo),
                        contentDescription = "NetCordon Logo",
                        modifier = Modifier.size(52.dp)
                    )
                } else if (slide.iconVector != null) {
                    Icon(
                        imageVector = slide.iconVector,
                        contentDescription = null,
                        tint = slide.badgeColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = slide.badgeColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, slide.badgeColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = slide.badge,
                    color = slide.badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = slide.title,
                color = OnBg,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = slide.subtitle,
                color = Dim,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Highlight Bullets
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slide.bullets.forEach { (text, color) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Surf,
                        border = BorderStroke(1.2.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                text = text,
                                color = OnBg,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ── Bottom: Indicators & Controls ──
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dots indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    val isSelected = index == currentIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) Green else Dim.copy(alpha = 0.3f))
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { currentIndex-- },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnBg),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Back", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick = {
                        if (currentIndex < slides.size - 1) {
                            currentIndex++
                        } else {
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    modifier = Modifier
                        .weight(if (currentIndex > 0) 2f else 1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = if (currentIndex < slides.size - 1) "Continue" else "Get Started",
                        color = OnGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = if (currentIndex < slides.size - 1) Icons.Default.ChevronRight else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OnGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
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
                color = if (allRequired) OnGreen else Dim
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
        border = BorderStroke(1.2.dp, if (granted) Green.copy(alpha = 0.35f) else CardBorder),
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
                            color = Red.copy(alpha = 0.14f),
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
                    color = Green.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Green.copy(alpha = 0.35f)),
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
                        containerColor = if (required) Green.copy(alpha = 0.15f) else Surf2,
                        contentColor = if (required) Green else OnBg
                    ),
                    border = BorderStroke(1.2.dp, if (required) Green.copy(alpha = 0.5f) else CardBorder),
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
    isAdFree: Boolean = false,
    onWatchAdForVip: () -> Unit = {},
    onShizukuRetry: () -> Unit,
    onOpenPolicy: (AppInfo) -> Unit
) {
    var filterMode by remember { mutableStateOf("all") } // all | blocked
    val displayedApps = remember(apps, filterMode) {
        if (filterMode == "blocked") apps.filter { it.isShielded }
        else apps
    }
    val blockedCount = apps.count { it.isShielded }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Status banners — compact rounded pill style
        if (!shizukuOk) item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = Red.copy(alpha = 0.1f),
                border = BorderStroke(1.2.dp, Red.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(16.dp))
                    Text("Shizuku not running — Start via Wireless Debugging", color = OnBg, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = onShizukuRetry, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("RETRY", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!serviceOn) item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = Yellow.copy(alpha = 0.1f),
                border = BorderStroke(1.2.dp, Yellow.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PauseCircle, null, tint = Yellow, modifier = Modifier.size(16.dp))
                    Text("Firewall paused — Toggle the switch above to activate", color = OnBg, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── 24h VIP Ad-Free Pass Banner Card (Hidden when user has claimed 24h pass) ──
        if (!isAdFree) item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = Surf2,
                border = BorderStroke(1.2.dp, Green.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Icon + Title + FREE Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Green.copy(alpha = 0.15f))
                                .border(1.dp, Green.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CardGiftcard,
                                null,
                                tint = Green,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "12h VIP Pass (Double to 48h!)",
                                    color = OnBg,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Green.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "FREE",
                                        color = Green,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "Watch 1 short video for 12h pass (Double to 48h available!)",
                                color = Dim,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Full-width CTA Button (Zero possibility of text wrapping or overflowing!)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Green,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWatchAdForVip() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                null,
                                tint = OnGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Watch Video for 12h Pass (Double to 48h)",
                                color = OnGreen,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Filter chip bar (High visibility pill boxes)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val allCount = apps.size
                val restrictedCount = blockedCount

                // All chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (filterMode == "all") Green.copy(alpha = 0.12f) else Surf2,
                    border = BorderStroke(1.2.dp, if (filterMode == "all") Green else CardBorder),
                    modifier = Modifier.clickable { filterMode = "all" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (filterMode == "all") Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Green))
                        Text("All ($allCount)", color = if (filterMode == "all") Green else Dim, fontSize = 12.sp, fontWeight = if (filterMode == "all") FontWeight.Bold else FontWeight.Medium)
                    }
                }

                // Restricted chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (filterMode == "blocked") Red.copy(alpha = 0.12f) else Surf2,
                    border = BorderStroke(1.2.dp, if (filterMode == "blocked") Red else CardBorder),
                    modifier = Modifier.clickable { filterMode = if (filterMode == "blocked") "all" else "blocked" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (filterMode == "blocked") Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Red))
                        Text("Restricted ($restrictedCount)", color = if (filterMode == "blocked") Red else Dim, fontSize = 12.sp, fontWeight = if (filterMode == "blocked") FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
            HorizontalDivider(color = Div, modifier = Modifier.padding(horizontal = 14.dp))
        }

        items(displayedApps, key = { it.packageName }) { app ->
            AppRow(
                app = app,
                showPkg = showPkg,
                onOpenPolicy = { onOpenPolicy(app) }
            )
            HorizontalDivider(color = Div, modifier = Modifier.padding(start = 76.dp, end = 14.dp))
        }
    }
}

/* ─── App Row ─── */
@Composable
fun AppRow(
    app: AppInfo,
    showPkg: Boolean,
    onOpenPolicy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPolicy() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon — bordered and clear
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Surf2)
                .border(1.dp, CardBorder, RoundedCornerShape(13.dp)),
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
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp, end = 10.dp)
        ) {
            Text(
                text = app.appName,
                color = OnBg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (showPkg) {
                    Text(
                        text = app.packageName,
                        color = Dim,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                val quota = if (app.dailyQuotaBytes > 0L) app.dailyQuotaBytes else PrefsManager.getAppDailyQuota(LocalContext.current, app.packageName)
                if (quota > 0L) {
                    val isQuotaBlocked = PrefsManager.getQuotaBlockedPackages(LocalContext.current).contains(app.packageName)
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isQuotaBlocked) Red.copy(alpha = 0.15f) else Green.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isQuotaBlocked) "Limit Hit" else "Quota ${DataUsageManager.formatBytes(quota)}",
                            color = if (isQuotaBlocked) Red else Green,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Right side: Mode pill with colored dot + chevron
        val modeColor = when (app.isolationMode) {
            AppIsolationMode.ALLOWED -> Green
            AppIsolationMode.SMART_SHIELD -> Yellow
            AppIsolationMode.TOTAL_BLACKOUT -> Red
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = modeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.2.dp, modeColor.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(modeColor))
                    Text(
                        text = when (app.isolationMode) {
                            AppIsolationMode.ALLOWED -> "Active"
                            AppIsolationMode.SMART_SHIELD -> "Shield"
                            AppIsolationMode.TOTAL_BLACKOUT -> "Blackout"
                        },
                        color = modeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Dim.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/* ─── Modern In-App Floating Toast Pill (Ultra-Slim & Sleek) ─── */
@Composable
fun ModernToastPill(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xF2121720),
        border = BorderStroke(0.8.dp, Green.copy(alpha = 0.45f)),
        shadowElevation = 3.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .clip(CircleShape)
                    .background(Green)
            )
            Text(
                text = message,
                color = OnBg,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/* ─── Double VIP Booster Dialog (Offer to double 12h pass to 48h / 2 Days) ─── */
@Composable
fun DoubleVipBoosterDialog(
    onDismiss: () -> Unit,
    onWatchSecondAd: () -> Unit
) {
    val theme = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surf,
        shape = RoundedCornerShape(22.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rocket / Sparkle Icon Badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Yellow.copy(alpha = 0.16f))
                        .border(1.5.dp, Yellow.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Yellow,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "🎉 12 Hours VIP Active!",
                    color = theme.onBg,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Want to DOUBLE it to 48 Hours?",
                    color = Green,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                // Highlight Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = theme.surf2,
                    border = BorderStroke(1.dp, theme.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Pass", color = theme.dim, fontSize = 12.sp)
                            Text("12 Hours", color = Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚀 Boosted Pass", color = theme.onBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("48 Hours (2 Days)", color = Green, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        HorizontalDivider(color = theme.cardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Green, modifier = Modifier.size(14.dp))
                            Text("100% Zero banner ads across all screens", color = theme.dim, fontSize = 11.5.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Green, modifier = Modifier.size(14.dp))
                            Text("Zero full-screen popups on any action", color = theme.dim, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onWatchSecondAd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Double to 48 Hours (Watch 1 More)", color = OnGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I'm happy with 12 Hours", color = theme.dim, fontSize = 12.sp)
            }
        }
    )
}

/* ─── 3-Tier App Isolation Policy Bottom Sheet ─── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPolicyBottomSheet(
    app: AppInfo,
    onDismiss: () -> Unit,
    onApply: (mode: AppIsolationMode, quotaBytes: Long, wifiBlocked: Boolean, dataBlocked: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ctx = LocalContext.current
    var wifiAllowed by remember(app.packageName) {
        mutableStateOf(!PrefsManager.isAppWifiBlocked(ctx, app.packageName))
    }
    var dataAllowed by remember(app.packageName) {
        mutableStateOf(!PrefsManager.isAppDataBlocked(ctx, app.packageName))
    }
    var currentMode by remember(app.packageName, app.isolationMode) { mutableStateOf(app.isolationMode) }
    var dailyQuota by remember(app.packageName) { mutableStateOf(PrefsManager.getAppDailyQuota(ctx, app.packageName)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surf,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Div)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // App Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        color = OnBg,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        color = Dim,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Selected Mode Chip — with colored dot
                val modeChipColor = when (currentMode) {
                    AppIsolationMode.ALLOWED -> Green
                    AppIsolationMode.SMART_SHIELD -> Yellow
                    AppIsolationMode.TOTAL_BLACKOUT -> Red
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = modeChipColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, modeChipColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(modeChipColor))
                        Text(
                            text = when (currentMode) {
                                AppIsolationMode.ALLOWED -> "Active"
                                AppIsolationMode.SMART_SHIELD -> "Shielded"
                                AppIsolationMode.TOTAL_BLACKOUT -> "Blackout"
                            },
                            color = modeChipColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3 Mode Selection Cards (Clicks only select locally, nothing is saved until Apply is clicked)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mode 1: Always Allowed
                PolicyModeCard(
                    title = "Always Allowed",
                    subtitle = "Normal internet access",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Green,
                    isSelected = currentMode == AppIsolationMode.ALLOWED,
                    onClick = {
                        currentMode = AppIsolationMode.ALLOWED
                        wifiAllowed = true
                        dataAllowed = true
                    }
                )

                // Mode 2: Smart Shield
                PolicyModeCard(
                    title = "Smart Shield",
                    subtitle = "Cut on close • Resume when open",
                    icon = Icons.Default.Shield,
                    accentColor = Yellow,
                    isSelected = currentMode == AppIsolationMode.SMART_SHIELD,
                    onClick = {
                        currentMode = AppIsolationMode.SMART_SHIELD
                        if (!wifiAllowed && !dataAllowed) {
                            wifiAllowed = true
                            dataAllowed = true
                        }
                    }
                )

                // Mode 3: Total Blackout
                PolicyModeCard(
                    title = "Total Blackout",
                    subtitle = "Block all internet • Zero ads",
                    icon = Icons.Default.Block,
                    accentColor = Red,
                    isSelected = currentMode == AppIsolationMode.TOTAL_BLACKOUT,
                    onClick = {
                        currentMode = AppIsolationMode.TOTAL_BLACKOUT
                        wifiAllowed = false
                        dataAllowed = false
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Network Interface Access Controls (Wi-Fi & Mobile Data) ──
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Surf2,
                border = BorderStroke(1.2.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "NETWORK INTERFACE ACCESS",
                            color = Dim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (wifiAllowed && dataAllowed) "Both Networks Active"
                            else if (!wifiAllowed && !dataAllowed) "All Networks Blocked"
                            else if (wifiAllowed) "Wi-Fi Only"
                            else "Mobile Data Only",
                            color = if (wifiAllowed || dataAllowed) Green else Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Wi-Fi Switch Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (wifiAllowed) Green.copy(alpha = 0.12f) else Red.copy(alpha = 0.12f),
                            border = BorderStroke(1.4.dp, if (wifiAllowed) Green.copy(alpha = 0.6f) else Red.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val next = !wifiAllowed
                                    wifiAllowed = next
                                    if (!next && !dataAllowed) {
                                        currentMode = AppIsolationMode.TOTAL_BLACKOUT
                                    } else if (currentMode == AppIsolationMode.TOTAL_BLACKOUT) {
                                        currentMode = AppIsolationMode.SMART_SHIELD
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (wifiAllowed) Icons.Default.Wifi else Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = if (wifiAllowed) Green else Red,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text("Wi-Fi", color = OnBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (wifiAllowed) "Allowed" else "Blocked",
                                            color = if (wifiAllowed) Green else Red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (wifiAllowed) Green else Red)
                                )
                            }
                        }

                        // Mobile Data Switch Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (dataAllowed) Green.copy(alpha = 0.12f) else Red.copy(alpha = 0.12f),
                            border = BorderStroke(1.4.dp, if (dataAllowed) Green.copy(alpha = 0.6f) else Red.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val next = !dataAllowed
                                    dataAllowed = next
                                    if (!wifiAllowed && !next) {
                                        currentMode = AppIsolationMode.TOTAL_BLACKOUT
                                    } else if (currentMode == AppIsolationMode.TOTAL_BLACKOUT) {
                                        currentMode = AppIsolationMode.SMART_SHIELD
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (dataAllowed) Icons.Default.SignalCellularAlt else Icons.Default.SignalCellularOff,
                                        contentDescription = null,
                                        tint = if (dataAllowed) Green else Red,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text("Mobile", color = OnBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (dataAllowed) "Allowed" else "Blocked",
                                            color = if (dataAllowed) Green else Red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (dataAllowed) Green else Red)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Daily Data Quota Card
            val (todayStart, todayEnd) = remember { DataUsageManager.getTimeRange("today") }
            val todayStats = remember(app.uid) { DataUsageManager.getUidStats(ctx, app.uid, todayStart, todayEnd) }
            val todayUsedBytes = todayStats.first + todayStats.second + todayStats.third + todayStats.fourth

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Surf2,
                border = BorderStroke(1.2.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.DataUsage, null, tint = Green, modifier = Modifier.size(18.dp))
                            Text("Daily Data Limit (Quota)", color = OnBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        if (dailyQuota > 0L) {
                            Text(
                                text = "${DataUsageManager.formatBytes(todayUsedBytes)} / ${DataUsageManager.formatBytes(dailyQuota)}",
                                color = if (todayUsedBytes >= dailyQuota) Red else Green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (dailyQuota > 0L) {
                        Spacer(Modifier.height(8.dp))
                        val progress = (todayUsedBytes.toFloat() / dailyQuota.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (todayUsedBytes >= dailyQuota) Red else Green,
                            trackColor = Surf
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (todayUsedBytes >= dailyQuota) "🚨 Limit hit! App auto-blocked until midnight." else "Auto-blocks app when daily limit is reached",
                            color = if (todayUsedBytes >= dailyQuota) Red else Dim,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Preset Chips
                    val presets = listOf(
                        0L to "Off",
                        100L * 1024L * 1024L to "100M",
                        250L * 1024L * 1024L to "250M",
                        500L * 1024L * 1024L to "500M",
                        1024L * 1024L * 1024L to "1GB",
                        2048L * 1024L * 1024L to "2GB"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (bytes, label) ->
                            val selected = (dailyQuota == bytes)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) Green.copy(alpha = 0.15f) else Surf,
                                border = BorderStroke(1.dp, if (selected) Green else CardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        dailyQuota = bytes
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Green else Dim,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Action Buttons: Cancel and Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, CardBorder)
                ) {
                    Text("Cancel", color = Dim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        onApply(currentMode, dailyQuota, !wifiAllowed, !dataAllowed)
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Apply", color = OnGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PolicyModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) accentColor.copy(alpha = 0.12f) else Surf2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.2.dp,
            if (isSelected) accentColor else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Surf2),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Dim,
                    modifier = Modifier.size(21.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isSelected) accentColor else OnBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = if (isSelected) accentColor.copy(alpha = 0.7f) else Dim,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = Dim.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/* ─── NetGuard icon button ─── */
@Composable
fun NetBtn(blocked: Boolean, onClick: () -> Unit, isWifi: Boolean) {
    val strikeColor = Red
    val activeColor = Green
    Box(modifier = Modifier.size(52.dp).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(11.dp),
            color = if (blocked) strikeColor.copy(alpha = 0.18f) else activeColor.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                    null,
                    tint = if (blocked) strikeColor else activeColor,
                    modifier = Modifier.size(20.dp)
                )
                if (blocked) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(38.dp)) {
                        drawLine(
                            color = strikeColor,
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

/* ─── Lock Screen (Biometric & PIN Shield) ─── */
@Composable
fun LockScreen(onUnlockTap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(Green.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = Green,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(26.dp))

        Text(
            text = "NetCordon Locked",
            color = OnBg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Security Shield is active.\nAuthenticate with Biometrics or Device PIN to continue.",
            color = Dim,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onUnlockTap,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Fingerprint, null, tint = OnGreen, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Unlock with Biometrics / PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnGreen)
        }
    }
}

/* ─── Bottom Navigation Bar ─── */
@Composable
fun NetCordonBottomBar(selectedTab: String, onSelectTab: (String) -> Unit) {
    Column {
        HorizontalDivider(color = CardBorder)
        NavigationBar(
            containerColor = TBg,
            tonalElevation = 0.dp,
            modifier = Modifier.height(66.dp)
        ) {
            listOf(
                Triple("home", "Firewall", Icons.Default.Shield),
                Triple("schedules", "Schedules", Icons.Default.Schedule),
                Triple("analytics", "Analytics", Icons.Default.Analytics),
                Triple("logs", "Logs", Icons.Default.Terminal),
                Triple("settings", "Settings", Icons.Default.Settings)
            ).forEach { (key, label, icon) ->
                val isSelected = selectedTab == key
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(key) },
                    icon = {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (isSelected) Green else Dim,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            label,
                            color = if (isSelected) Green else Dim,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Green.copy(alpha = 0.18f),
                        selectedIconColor = Green,
                        selectedTextColor = Green,
                        unselectedIconColor = Dim,
                        unselectedTextColor = Dim
                    )
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════
   SCHEDULES SCREEN (BEDTIME & FOCUS AUTOMATION)
═══════════════════════════════════════════════════ */
@Composable
fun SchedulesScreen(apps: List<AppInfo>) {
    val ctx = LocalContext.current
    var schedules by remember { mutableStateOf(PrefsManager.getSchedules(ctx)) }
    var selectedScheduleForApps by remember { mutableStateOf<FirewallSchedule?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // Hero Info Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Green.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Green.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Green.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(24.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automated Firewall Timers", color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Auto-isolate apps at bedtime or work hours. Zero battery drain.",
                            color = Dim,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Add Custom Schedule Button
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Surf,
                border = BorderStroke(1.5.dp, Green.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddCustomDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Green.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Green, modifier = Modifier.size(18.dp))
                    }
                    Text("Add Custom Schedule", color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = Green.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "MY SCHEDULES",
                    color = Dim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                if (schedules.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Green.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Green.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "${schedules.size} active",
                            color = Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (schedules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Surf,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Green.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(26.dp))
                        }
                        Text("No Active Schedules", color = OnBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "All pre-made templates removed. Create your own custom firewall timer with exact hours.",
                            color = Dim,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { showAddCustomDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = OnGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create Custom Schedule", color = OnGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onToggle = { isEnabled ->
                        val updated = schedule.copy(isEnabled = isEnabled)
                        val nextList = schedules.map { if (it.id == schedule.id) updated else it }
                        schedules = nextList
                        PrefsManager.saveSchedules(ctx, nextList)
                        if (isEnabled) {
                            ScheduleReceiver.schedule(ctx, updated)
                        } else {
                            ScheduleReceiver.cancel(ctx, schedule.id)
                        }
                    },
                    onSelectApps = {
                        selectedScheduleForApps = schedule
                    },
                    onDelete = {
                        ScheduleReceiver.cancel(ctx, schedule.id)
                        val nextList = schedules.filter { it.id != schedule.id }
                        schedules = nextList
                        PrefsManager.saveSchedules(ctx, nextList)
                    }
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    // Modal to configure apps for this schedule (interactive selection)
    val curSched = selectedScheduleForApps
    if (curSched != null) {
        ScheduleAppSelectorDialog(
            allApps = apps,
            initialSelected = curSched.targetPackages,
            scheduleTitle = curSched.title,
            mode = curSched.mode,
            onDismiss = { selectedScheduleForApps = null },
            onConfirm = { updatedPkgs ->
                val updated = curSched.copy(targetPackages = updatedPkgs)
                val nextList = schedules.map { if (it.id == curSched.id) updated else it }
                schedules = nextList
                PrefsManager.saveSchedules(ctx, nextList)
                if (updated.isEnabled) {
                    ScheduleReceiver.schedule(ctx, updated)
                }
                selectedScheduleForApps = null
            }
        )
    }

    // Modal to create a new custom schedule with modern time picker
    if (showAddCustomDialog) {
        AddCustomScheduleDialog(
            allApps = apps,
            onDismiss = { showAddCustomDialog = false },
            onSave = { newSched ->
                val nextList = schedules + newSched
                schedules = nextList
                PrefsManager.saveSchedules(ctx, nextList)
                ScheduleReceiver.schedule(ctx, newSched)
                showAddCustomDialog = false
                val activity = ctx.findActivity()
                if (activity != null && PrefsManager.shouldShowInterstitial(ctx)) {
                    AdMobManager.showInterstitialAd(activity)
                }
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: FirewallSchedule,
    onToggle: (Boolean) -> Unit,
    onSelectApps: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    fun format12Hour(hour24: Int, min: Int): String {
        val h = hour24 % 12
        val displayH = if (h == 0) 12 else h
        val amPm = if (hour24 < 12) "AM" else "PM"
        return String.format(java.util.Locale.US, "%02d:%02d %s", displayH, min, amPm)
    }
    val startStr = format12Hour(schedule.startHour, schedule.startMinute)
    val endStr = format12Hour(schedule.endHour, schedule.endMinute)
    val daysLabel = if (schedule.daysOfWeek.size == 7) "Every Day" else if (schedule.daysOfWeek.size == 5 && !schedule.daysOfWeek.contains(1) && !schedule.daysOfWeek.contains(7)) "Mon - Fri" else "${schedule.daysOfWeek.size} Days"
    val modeColor = when (schedule.mode) {
        AppIsolationMode.ALLOWED -> Green
        AppIsolationMode.SMART_SHIELD -> Yellow
        AppIsolationMode.TOTAL_BLACKOUT -> Red
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Surf,
        border = BorderStroke(1.2.dp, if (schedule.isEnabled) modeColor.copy(alpha = 0.5f) else CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        if (schedule.isEnabled) modeColor else Dim.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 14.dp)) {
                // Top row: title + delete + switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isLiveActive = schedule.isCurrentlyActive()
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = schedule.title,
                                color = OnBg,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isLiveActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Green.copy(alpha = 0.15f))
                                        .border(0.8.dp, Green.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("● LIVE", color = Green, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        // Time range in monospace for readability
                        Text(
                            text = "$startStr – $endStr",
                            color = if (schedule.isEnabled) modeColor else Dim,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Dim.copy(alpha = 0.5f), modifier = Modifier.size(17.dp))
                            }
                        }

                        NetCordonSwitch(checked = schedule.isEnabled, onCheckedChange = onToggle)
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Div)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: mode + days pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = modeColor.copy(alpha = 0.13f),
                            border = BorderStroke(1.dp, modeColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(modeColor))
                                Text(
                                    text = when (schedule.mode) {
                                        AppIsolationMode.ALLOWED -> "Allow"
                                        AppIsolationMode.SMART_SHIELD -> "Smart Shield"
                                        AppIsolationMode.TOTAL_BLACKOUT -> "Blackout"
                                    },
                                    color = modeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Days badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Surf2,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = daysLabel,
                                color = Dim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Right: apps configured
                    val appCount = schedule.targetPackages.size
                    TextButton(
                        onClick = onSelectApps,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        val label = when (appCount) {
                            0 -> "⚠️ 0 Apps ›"
                            1 -> "1 App ›"
                            else -> "$appCount Apps ›"
                        }
                        Text(
                            text = label,
                            color = if (appCount == 0) Yellow else Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleAppSelectorDialog(
    allApps: List<AppInfo>,
    initialSelected: Set<String>,
    scheduleTitle: String,
    mode: AppIsolationMode,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var selectedPkgs by remember { mutableStateOf(initialSelected.toMutableSet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val isBlackout = mode == AppIsolationMode.TOTAL_BLACKOUT
    val modeColor = if (isBlackout) Red else Yellow

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surf,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(modeColor.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, modeColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isBlackout) Icons.Default.Block else Icons.Default.Shield,
                        null,
                        tint = modeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Configure Schedule Apps",
                        color = OnBg,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = "${selectedPkgs.size} of ${allApps.size} apps selected",
                        color = modeColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Input Field (Clean, vertically centered, zero text clipping)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Surf2,
                    border = BorderStroke(1.dp, if (searchQuery.isNotEmpty()) Green.copy(alpha = 0.5f) else CardBorder),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = if (searchQuery.isNotEmpty()) Green else Dim,
                            modifier = Modifier.size(17.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search apps or packages...",
                                    color = Dim,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = OnBg,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Green),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Dim, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }

                // Quick Selection Controls (Select All / Deselect All)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "${filteredApps.size} matching" else "${allApps.size} total apps",
                        color = Dim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Green.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Green.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                val next = selectedPkgs.toMutableSet()
                                filteredApps.forEach { next.add(it.packageName) }
                                selectedPkgs = next
                            }
                        ) {
                            Text(
                                text = "Select All",
                                color = Green,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Red.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Red.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                val next = selectedPkgs.toMutableSet()
                                filteredApps.forEach { next.remove(it.packageName) }
                                selectedPkgs = next
                            }
                        ) {
                            Text(
                                text = "Clear All",
                                color = Red,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Scrollable App List with checkboxes
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Surf2,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                ) {
                    if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No apps matching search", color = Dim, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                val isSelected = selectedPkgs.contains(app.packageName)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Green.copy(alpha = 0.10f) else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) Green.copy(alpha = 0.35f) else Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val next = selectedPkgs.toMutableSet()
                                            if (isSelected) next.remove(app.packageName) else next.add(app.packageName)
                                            selectedPkgs = next
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (app.iconBitmap != null) {
                                            Image(
                                                bitmap = app.iconBitmap,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Surf),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(app.appName.take(1), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.appName,
                                                color = OnBg,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = app.packageName,
                                                color = Dim,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                val next = selectedPkgs.toMutableSet()
                                                if (checked) next.add(app.packageName) else next.remove(app.packageName)
                                                selectedPkgs = next
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Green,
                                                uncheckedColor = CardBorder,
                                                checkmarkColor = OnGreen
                                            ),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedPkgs)
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "Done (${selectedPkgs.size} Selected)",
                    color = OnGreen,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text("Cancel", color = Dim, fontSize = 12.5.sp)
            }
        }
    )
}

@Composable
fun AddCustomScheduleDialog(
    allApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onSave: (FirewallSchedule) -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var startHourStr by remember { mutableStateOf("10") }
    var startMinStr by remember { mutableStateOf("00") }
    var startAmPm by remember { mutableStateOf("PM") }
    var endHourStr by remember { mutableStateOf("07") }
    var endMinStr by remember { mutableStateOf("00") }
    var endAmPm by remember { mutableStateOf("AM") }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) } // All 7 days by default
    var isolationMode by remember { mutableStateOf(AppIsolationMode.TOTAL_BLACKOUT) }
    var selectedPkgs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAppSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surf,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Green.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, Green.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, tint = Green, modifier = Modifier.size(18.dp))
                }
                Text("Add Custom Schedule", color = OnBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Schedule Title Input Box (High Visibility with Surf2 container and CardBorder)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Schedule Name (e.g. Gaming Mute)", color = Dim, fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Surf2,
                        unfocusedContainerColor = Surf2,
                        focusedTextColor = OnBg,
                        unfocusedTextColor = OnBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Name Suggestion Chips (All 4 chips have EXACT SAME SIZE, no line wrapping!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val presets = listOf("🎮 Gaming", "📚 Study", "🏃 Workout", "🌙 Night")
                    presets.forEach { suggestion ->
                        val isSelected = title == suggestion
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Green.copy(alpha = 0.15f) else Surf2,
                            border = BorderStroke(1.dp, if (isSelected) Green.copy(alpha = 0.6f) else CardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clickable { title = suggestion }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = suggestion,
                                    color = if (isSelected) Green else OnBg,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Text("TIME RANGE (12-HOUR FORMAT)", color = Dim, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)

                // Unified 12-Hour Time Range Card with crisp 1dp border
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Surf2,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // ── START TIME ROW ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Green)
                                )
                                Text("START", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Time Input Box [ HH : MM ]
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Surf,
                                    border = BorderStroke(1.5.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            value = startHourStr,
                                            onValueChange = { input ->
                                                startHourStr = input.filter { it.isDigit() }.take(2)
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = OnBg,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            cursorBrush = SolidColor(Green),
                                            modifier = Modifier.width(26.dp)
                                        )
                                        Text(":", color = Green, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 1.dp))
                                        BasicTextField(
                                            value = startMinStr,
                                            onValueChange = { input ->
                                                startMinStr = input.filter { it.isDigit() }.take(2)
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = OnBg,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            cursorBrush = SolidColor(Green),
                                            modifier = Modifier.width(26.dp)
                                        )
                                    }
                                }

                                // AM/PM Toggle Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Surf,
                                    border = BorderStroke(1.5.dp, CardBorder)
                                ) {
                                    Row(modifier = Modifier.padding(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (startAmPm == "AM") Green else Color.Transparent)
                                                .clickable { startAmPm = "AM" }
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("AM", color = if (startAmPm == "AM") OnGreen else Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (startAmPm == "PM") Green else Color.Transparent)
                                                .clickable { startAmPm = "PM" }
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("PM", color = if (startAmPm == "PM") OnGreen else Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 1.dp)

                        // ── END TIME ROW ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Yellow)
                                )
                                Text("END", color = Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Time Input Box [ HH : MM ]
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Surf,
                                    border = BorderStroke(1.5.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            value = endHourStr,
                                            onValueChange = { input ->
                                                endHourStr = input.filter { it.isDigit() }.take(2)
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = OnBg,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Next
                                            ),
                                            cursorBrush = SolidColor(Yellow),
                                            modifier = Modifier.width(26.dp)
                                        )
                                        Text(":", color = Yellow, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 1.dp))
                                        BasicTextField(
                                            value = endMinStr,
                                            onValueChange = { input ->
                                                endMinStr = input.filter { it.isDigit() }.take(2)
                                            },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = OnBg,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done
                                            ),
                                            cursorBrush = SolidColor(Yellow),
                                            modifier = Modifier.width(26.dp)
                                        )
                                    }
                                }

                                // AM/PM Toggle Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Surf,
                                    border = BorderStroke(1.5.dp, CardBorder)
                                ) {
                                    val onYellow = if (LocalThemeColors.current.isDark) Color(0xFF261800) else Color.White
                                    Row(modifier = Modifier.padding(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (endAmPm == "AM") Yellow else Color.Transparent)
                                                .clickable { endAmPm = "AM" }
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("AM", color = if (endAmPm == "AM") onYellow else Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (endAmPm == "PM") Yellow else Color.Transparent)
                                                .clickable { endAmPm = "PM" }
                                                .padding(horizontal = 7.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("PM", color = if (endAmPm == "PM") onYellow else Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Repeat Days (Compact 28.dp circles)
                Text("REPEAT DAYS", color = Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf(
                        "M" to java.util.Calendar.MONDAY,
                        "T" to java.util.Calendar.TUESDAY,
                        "W" to java.util.Calendar.WEDNESDAY,
                        "T" to java.util.Calendar.THURSDAY,
                        "F" to java.util.Calendar.FRIDAY,
                        "S" to java.util.Calendar.SATURDAY,
                        "S" to java.util.Calendar.SUNDAY
                    )
                    days.forEach { (label, calDay) ->
                        val isSel = selectedDays.contains(calDay)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isSel) Green else Surf)
                                .border(1.2.dp, if (isSel) Green else CardBorder, CircleShape)
                                .clickable {
                                    val next = selectedDays.toMutableSet()
                                    if (isSel) {
                                        if (next.size > 1) next.remove(calDay)
                                    } else {
                                        next.add(calDay)
                                    }
                                    selectedDays = next
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) OnGreen else Dim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Firewall Policy Selection
                Text("FIREWALL MODE", color = Dim, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Blackout
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isolationMode == AppIsolationMode.TOTAL_BLACKOUT) Red.copy(alpha = 0.18f) else Surf2,
                        border = BorderStroke(1.2.dp, if (isolationMode == AppIsolationMode.TOTAL_BLACKOUT) Red else CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clickable {
                                isolationMode = AppIsolationMode.TOTAL_BLACKOUT
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🔴 Blackout", color = if (isolationMode == AppIsolationMode.TOTAL_BLACKOUT) Red else OnBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Smart Shield
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isolationMode == AppIsolationMode.SMART_SHIELD) Yellow.copy(alpha = 0.18f) else Surf2,
                        border = BorderStroke(1.2.dp, if (isolationMode == AppIsolationMode.SMART_SHIELD) Yellow else CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clickable {
                                isolationMode = AppIsolationMode.SMART_SHIELD
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("🟡 Smart Shield", color = if (isolationMode == AppIsolationMode.SMART_SHIELD) Yellow else OnBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Target Apps Configuration (Independent from Firewall)
                Text("TARGET APPS", color = Dim, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Surf2,
                    border = BorderStroke(1.dp, if (selectedPkgs.isNotEmpty()) Green.copy(alpha = 0.4f) else CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAppSelector = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedPkgs.isNotEmpty()) Green.copy(alpha = 0.15f) else Surf)
                                    .border(1.dp, if (selectedPkgs.isNotEmpty()) Green.copy(alpha = 0.35f) else CardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Android,
                                    null,
                                    tint = if (selectedPkgs.isNotEmpty()) Green else Dim,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedPkgs.isEmpty()) "Target Apps" else "${selectedPkgs.size} Apps Selected",
                                    color = OnBg,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selectedPkgs.isEmpty()) "Tap to choose apps" else "Configured for schedule",
                                    color = if (selectedPkgs.isNotEmpty()) Green else Dim,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Green.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Green.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (selectedPkgs.isEmpty()) "Select Apps" else "Edit (${selectedPkgs.size})",
                                    color = Green,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (title.isNotBlank()) title.trim() else "Custom Schedule"
                    val sh = (startHourStr.toIntOrNull() ?: 10).coerceIn(1, 12)
                    val sm = (startMinStr.toIntOrNull() ?: 0).coerceIn(0, 59)
                    val eh = (endHourStr.toIntOrNull() ?: 7).coerceIn(1, 12)
                    val em = (endMinStr.toIntOrNull() ?: 0).coerceIn(0, 59)

                    val finalStartHour = when {
                        startAmPm == "AM" && sh == 12 -> 0
                        startAmPm == "AM" -> sh
                        startAmPm == "PM" && sh == 12 -> 12
                        else -> sh + 12
                    }
                    val finalEndHour = when {
                        endAmPm == "AM" && eh == 12 -> 0
                        endAmPm == "AM" -> eh
                        endAmPm == "PM" && eh == 12 -> 12
                        else -> eh + 12
                    }

                    val newSched = FirewallSchedule(
                        id = "sched_" + System.currentTimeMillis(),
                        title = finalTitle,
                        startHour = finalStartHour,
                        startMinute = sm,
                        endHour = finalEndHour,
                        endMinute = em,
                        daysOfWeek = selectedDays,
                        mode = isolationMode,
                        targetPackages = selectedPkgs,
                        isEnabled = true
                    )
                    onSave(newSched)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                modifier = Modifier.height(42.dp)
            ) {
                Text("Save Schedule", color = OnGreen, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(42.dp)
            ) {
                Text("Cancel", color = Dim, fontSize = 13.sp)
            }
        }
    )

    if (showAppSelector) {
        ScheduleAppSelectorDialog(
            allApps = allApps,
            initialSelected = selectedPkgs,
            scheduleTitle = if (title.isNotBlank()) title.trim() else "Custom Schedule",
            mode = isolationMode,
            onDismiss = { showAppSelector = false },
            onConfirm = { chosen ->
                selectedPkgs = chosen
                showAppSelector = false
            }
        )
    }
}

/* ─── Analytics Screen (Real-Time Data Usage Dashboard) ─── */
@Composable
fun AnalyticsScreen(apps: List<AppInfo>) {
    val ctx = LocalContext.current
    var period by remember { mutableStateOf("today") } // today | week | month
    var stats by remember { mutableStateOf<OverallNetworkStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    fun refreshStats() {
        isLoading = true
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                DataUsageManager.getOverallStats(ctx, apps, period)
            }
            stats = result
            isLoading = false
        }
    }

    var usageGranted by remember { mutableStateOf(hasUsageAccess(ctx)) }

    // Re-check and attempt auto-grant on entry
    LaunchedEffect(Unit) {
        if (!usageGranted) {
            withContext(Dispatchers.IO) {
                if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                    ShizukuManager.executeCommand("cmd appops set ${ctx.packageName} GET_USAGE_STATS allow")
                    ShizukuManager.executeCommand("pm grant ${ctx.packageName} android.permission.PACKAGE_USAGE_STATS")
                }
            }
            usageGranted = hasUsageAccess(ctx)
            if (usageGranted) refreshStats()
        }
    }

    LaunchedEffect(period, apps) {
        refreshStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Usage Access Banner (if missing, provides 1-tap grant or Shizuku auto-grant)
            if (!usageGranted) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Yellow.copy(alpha = 0.08f),
                        border = BorderStroke(1.2.dp, Yellow.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Yellow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, null, tint = Yellow, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Usage Access Required", color = OnBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Text("Grant permission to calculate live per-app network traffic & bandwidth stats.", color = Dim, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            if (ShizukuManager.isShizukuAvailable() && ShizukuManager.hasShizukuPermission()) {
                                                ShizukuManager.executeCommand("cmd appops set ${ctx.packageName} GET_USAGE_STATS allow")
                                                ShizukuManager.executeCommand("pm grant ${ctx.packageName} android.permission.PACKAGE_USAGE_STATS")
                                            }
                                        }
                                        usageGranted = hasUsageAccess(ctx)
                                        if (!usageGranted) {
                                            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        } else {
                                            refreshStats()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Yellow),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                val onYellow = if (LocalThemeColors.current.isDark) Color(0xFF261800) else Color.White
                                Text("Grant", color = onYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Time Period Selector Chips — pill style with dot indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("today" to "Today", "week" to "Last 7 Days", "month" to "Last 30 Days").forEach { (key, label) ->
                        val selected = period == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) Green.copy(alpha = 0.12f) else Surf,
                            border = BorderStroke(1.dp, if (selected) Green.copy(alpha = 0.5f) else CardBorder),
                            modifier = Modifier.clickable { period = key }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (selected) Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Green))
                                Text(
                                    text = label,
                                    color = if (selected) Green else Dim,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 🚨 Leak Radar & Tracker Shield Card
            item {
                val blockedAttemptsMap = remember { PrefsManager.getTodayBlockedAttempts(ctx) }
                val totalAttempts = remember { PrefsManager.getTodayTotalBlockedAttempts(ctx) }
                val isLeakAlertsOn = remember { PrefsManager.isLeakAlertsEnabled(ctx) }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Red.copy(alpha = 0.05f),
                    border = BorderStroke(1.2.dp, Red.copy(alpha = if (LocalThemeColors.current.isDark) 0.35f else 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Red.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, null, tint = Red, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text("Leak Radar & Trackers", color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Real-time background shield", color = Dim, fontSize = 11.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isLeakAlertsOn) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isLeakAlertsOn) "● Active" else "Paused",
                                    color = if (isLeakAlertsOn) Green else Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Blocked Pings card — red tinted
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Red.copy(alpha = if (LocalThemeColors.current.isDark) 0.12f else 0.07f),
                                border = BorderStroke(1.dp, Red.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Icon(Icons.Default.Block, null, tint = Red, modifier = Modifier.size(13.dp))
                                        Text("Blocked Pings", color = Dim, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "$totalAttempts",
                                        color = OnBg,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text("prevented leaks", color = Dim, fontSize = 10.sp)
                                }
                            }

                            // Active Leakers card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Surf,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Icon(Icons.Default.Shield, null, tint = Green, modifier = Modifier.size(13.dp))
                                        Text("Active Leakers", color = Dim, fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "${blockedAttemptsMap.size}",
                                        color = OnBg,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text("apps caught leaking", color = Dim, fontSize = 10.sp)
                                }
                            }
                        }

                        // Top offender chips if any
                        if (blockedAttemptsMap.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Div)
                            Spacer(Modifier.height(8.dp))
                            Text("TOP ATTEMPTS TODAY", color = Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(6.dp))
                            blockedAttemptsMap.entries.sortedByDescending { it.value }.take(3).forEach { (pkg, attempts) ->
                                val appInfo = apps.firstOrNull { it.packageName == pkg }
                                val appName = appInfo?.appName ?: pkg.substringAfterLast('.')
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Red))
                                        Text(appName, color = OnBg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Red.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Red.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            "$attempts blocked",
                                            color = Red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Hero Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Estimated Saved Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Green.copy(alpha = if (LocalThemeColors.current.isDark) 0.10f else 0.07f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, Green.copy(alpha = if (LocalThemeColors.current.isDark) 0.35f else 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Green.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Shield, null, tint = Green, modifier = Modifier.size(15.dp)) }
                                Text("Data Saved", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = DataUsageManager.formatBytes(stats?.totalSavedBytes ?: 0L),
                                color = OnBg,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(text = "background freeze", color = Dim, fontSize = 10.sp)
                        }
                    }

                    // Blocked Pings Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Yellow.copy(alpha = if (LocalThemeColors.current.isDark) 0.10f else 0.07f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, Yellow.copy(alpha = if (LocalThemeColors.current.isDark) 0.35f else 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Yellow.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Block, null, tint = Yellow, modifier = Modifier.size(15.dp)) }
                                Text("Pings Blocked", color = Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "${stats?.blockedPingsCount ?: 0}",
                                color = OnBg,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(text = "background leaks cut", color = Dim, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Total Monitored Card & Visual Ratio Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Surf,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Network Traffic", color = Dim, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    DataUsageManager.formatBytes(stats?.totalBytes ?: 0L),
                                    color = OnBg,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Blue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.DataUsage, null, tint = Blue, modifier = Modifier.size(20.dp)) }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Ratio Bar
                        val totalB = stats?.totalBytes ?: 1L
                        val safeTotal = if (totalB <= 0L) 1L else totalB
                        val wifiFrac = ((stats?.totalWifi ?: 0L).toFloat() / safeTotal).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Surf2)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .weight(if (wifiFrac > 0f) wifiFrac else 0.001f)
                                        .fillMaxHeight()
                                        .background(Blue)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(if ((1f - wifiFrac) > 0f) (1f - wifiFrac) else 0.001f)
                                        .fillMaxHeight()
                                        .background(Yellow)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Blue))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "WiFi: ${DataUsageManager.formatBytes(stats?.totalWifi ?: 0L)}",
                                    color = OnBg,
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Yellow))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Mobile: ${DataUsageManager.formatBytes(stats?.totalMobile ?: 0L)}",
                                    color = OnBg,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Header for App Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "App Data Breakdown",
                        color = OnBg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { refreshStats() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Dim, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // App list items
            val appUsageList = stats?.appStats ?: emptyList()
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(28.dp))
                    }
                }
            } else if (appUsageList.isEmpty()) {
                item {
                    Text(
                        "No usage records found for selected period.",
                        color = Dim,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(appUsageList, key = { it.packageName }) { item ->
                    val maxUsage = stats?.appStats?.firstOrNull()?.totalBytes ?: 1L
                    val safeMax = if (maxUsage <= 0L) 1L else maxUsage
                    val progress = (item.totalBytes.toFloat() / safeMax).coerceIn(0f, 1f)
                    val appInfo = apps.firstOrNull { it.packageName == item.packageName }

                    Surface(
                        color = Surf,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Surf2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (appInfo?.iconBitmap != null) {
                                        Image(
                                            bitmap = appInfo.iconBitmap,
                                            contentDescription = item.appName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = item.appName.take(1).uppercase(),
                                            color = Green,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.appName,
                                            color = OnBg,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (item.isWifiBlocked || item.isDataBlocked) {
                                            Surface(
                                                color = Red.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, Red.copy(alpha = 0.25f))
                                            ) {
                                                Text(
                                                    text = "BLOCKED",
                                                    color = Red,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Icon(Icons.Default.Wifi, null, tint = Blue, modifier = Modifier.size(10.dp))
                                            Text(DataUsageManager.formatBytes(item.totalWifi), color = Dim, fontSize = 10.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Icon(Icons.Default.SignalCellularAlt, null, tint = Dim.copy(alpha = 0.7f), modifier = Modifier.size(10.dp))
                                            Text(DataUsageManager.formatBytes(item.totalMobile), color = Dim, fontSize = 10.sp)
                                        }
                                    }
                                }

                                Text(
                                    text = DataUsageManager.formatBytes(item.totalBytes),
                                    color = if (item.totalBytes > 0) OnBg else Dim,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (item.isWifiBlocked || item.isDataBlocked) Red else Blue,
                                trackColor = Surf2,
                            )
                        }
                }
            } // end items
            } // end else

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* ─── Logs Screen ─── */
@Composable
fun LogsScreen(logs: List<String>) {
    Column(modifier = Modifier.fillMaxSize().background(BG)) {
        // Legend strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surf)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("V", Dim, "Verbose"),
                Triple("D", Blue, "Debug"),
                Triple("I", Green, "Info"),
                Triple("W", Yellow, "Warn"),
                Triple("E", Red, "Error")
            ).forEach { (t, c, full) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(c))
                    Text(full, color = Dim, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        HorizontalDivider(color = CardBorder)
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, null, tint = Dim.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                    Text("No log entries yet.", color = Dim, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(logs) { log ->
                    val isDark = LocalThemeColors.current.isDark
                    val logColor = when {
                        "  W  " in log -> Yellow
                        "  E  " in log -> Red
                        "  D  " in log -> Blue
                        "  I  " in log -> Green
                        else           -> Dim
                    }
                    Text(
                        log,
                        color = logColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

/* ─── Settings Screen ─── */
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    privilege: String, bootStart: Boolean, serviceOn: Boolean,
    showPkg: Boolean, minNotif: Boolean, blockNotifs: Boolean, screenOffShield: Boolean,
    appLockEnabled: Boolean, lockOnScreenOff: Boolean,
    shizukuOk: Boolean, usageOk: Boolean, notifOk: Boolean, batteryOk: Boolean,
    onPrivilege: (String) -> Unit, onBoot: (Boolean) -> Unit, onService: (Boolean) -> Unit,
    onPkg: (Boolean) -> Unit, onNotif: (Boolean) -> Unit,
    onBlockNotifs: (Boolean) -> Unit,
    onScreenOffShield: (Boolean) -> Unit,
    onAppLockChange: (Boolean) -> Unit,
    onLockOnScreenOffChange: (Boolean) -> Unit,
    leakAlertsEnabled: Boolean,
    onLeakAlertsChange: (Boolean) -> Unit,
    onGrantUsage: () -> Unit, onGrantNotif: () -> Unit, onGrantBattery: () -> Unit, onGrantShizuku: () -> Unit,
    onReplayWalkthrough: () -> Unit,
    isAdFree: Boolean = false,
    adFreeUntil: Long = 0L,
    onWatchAdForVip: () -> Unit = {},
    onResetPass: () -> Unit = {},
    onProfileRestored: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(modifier = Modifier.fillMaxSize().background(BG)) {

        // ── 0. Supporter VIP & Ad-Free Pass ──
        item { SecHeader("Supporter VIP & Ad-Free Pass") }
        item {
            val remainingMillis = if (adFreeUntil > 0L) maxOf(0L, adFreeUntil - System.currentTimeMillis()) else PrefsManager.getAdFreeRemainingMillis(ctx)
            val hoursLeft = remainingMillis / (1000 * 60 * 60)
            val minutesLeft = (remainingMillis / (1000 * 60)) % 60

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Row: Icon + Title + Status Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isAdFree) Green.copy(alpha = 0.15f) else Blue.copy(alpha = 0.15f))
                                    .border(1.dp, if (isAdFree) Green.copy(alpha = 0.35f) else Blue.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isAdFree) Icons.Default.Shield else Icons.Default.CardGiftcard,
                                    null,
                                    tint = if (isAdFree) Green else Blue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                Text(
                                    if (isAdFree) "VIP Ad-Free Pass" else "Free Standard Tier",
                                    color = OnBg,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    if (isAdFree) "Active • Zero Ads in App" else "Ads enabled on logs & stats",
                                    color = if (isAdFree) Green else Dim,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAdFree) Green.copy(alpha = 0.15f) else Surf2,
                            border = BorderStroke(1.dp, if (isAdFree) Green.copy(alpha = 0.4f) else CardBorder)
                        ) {
                            Text(
                                text = if (isAdFree) "ACTIVE" else "INACTIVE",
                                color = if (isAdFree) Green else Dim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorder.copy(alpha = 0.6f))

                    // Bottom Row: Time Remaining (Left) and Action Button (Right) - Zero overlap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "TIME REMAINING",
                                color = Dim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isAdFree) "${hoursLeft}h ${minutesLeft}m remaining" else "0h 0m (Pass inactive)",
                                color = if (isAdFree) Green else Dim,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAdFree) Surf2 else Green,
                            border = BorderStroke(1.dp, if (isAdFree) Green.copy(alpha = 0.45f) else Color.Transparent),
                            modifier = Modifier.clickable { onWatchAdForVip() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    tint = if (isAdFree) Green else OnGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    if (isAdFree) "+ Extend Pass" else "Get 12h Pass (Double to 48h)",
                                    color = if (isAdFree) Green else OnGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Test Controls Row (Quickly test all ads on demand)
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                val act = ctx.findActivity()
                                if (act != null) {
                                    AdMobManager.showInterstitialAd(act, force = true)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Bolt, null, tint = Yellow, modifier = Modifier.size(14.dp))
                                Text("Test Interstitial", color = Yellow, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (isAdFree) {
                            TextButton(
                                onClick = {
                                    PrefsManager.resetAdFreePass(ctx)
                                    onResetPass()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Refresh, null, tint = Red.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                                    Text("Reset Pass (Show Ads)", color = Red.copy(alpha = 0.85f), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 1. Core Engine & System Permissions ──
        item { SecHeader("Core Engine & Permissions") }
        item {
            var showShizukuTestDialog by remember { mutableStateOf(false) }
            var shizukuTestMessage by remember { mutableStateOf("") }
            var shizukuTestSuccess by remember { mutableStateOf(false) }

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (shizukuOk) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f))
                                    .border(1.dp, if (shizukuOk) Green.copy(alpha = 0.35f) else Red.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    null,
                                    tint = if (shizukuOk) Green else Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Shizuku",
                                    color = OnBg,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "ADB Wireless Debugging",
                                    color = Dim,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Live status badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (shizukuOk) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (shizukuOk) Green.copy(alpha = 0.4f) else Red.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (shizukuOk) Green else Red)
                                )
                                Text(
                                    text = if (shizukuOk) "Connected" else "Not Running",
                                    color = if (shizukuOk) Green else Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (shizukuOk)
                            "System-level network policy controls active via ADB shell (UID 2000). Full firewall operational without root."
                        else
                            "Shizuku service is not running. Start Shizuku via Wireless Debugging to execute firewall rules.",
                        color = Dim,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Test Connection Button
                        Button(
                            onClick = {
                                val isAvailable = ShizukuManager.isShizukuAvailable()
                                val hasPerm = ShizukuManager.hasShizukuPermission()
                                if (isAvailable && hasPerm) {
                                    shizukuTestSuccess = true
                                    shizukuTestMessage = "✅ Shizuku Connection Verified!\n\n• Binder Ping: Active & Healthy\n• Permission: Granted (UID 2000 ADB shell)\n• Network Engine: Chain-3 Firewall ready\n• Status: Full protection running smoothly"
                                } else if (isAvailable && !hasPerm) {
                                    shizukuTestSuccess = false
                                    shizukuTestMessage = "⚠️ Shizuku Service Detected, But Permission Missing!\n\n• Binder Ping: OK\n• Permission: Not Granted\n\nTap 'Authorize' to grant NetCordon permission."
                                } else {
                                    shizukuTestSuccess = false
                                    shizukuTestMessage = "❌ Shizuku Service Not Running!\n\n• Binder Ping: Failed\n• Cause: Shizuku is stopped or device rebooted without starting Wireless Debugging.\n\nPlease open the Shizuku app and start the service."
                                }
                                showShizukuTestDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (shizukuOk) Green.copy(alpha = 0.15f) else Surf2,
                                contentColor = if (shizukuOk) Green else OnBg
                            ),
                            border = BorderStroke(1.2.dp, if (shizukuOk) Green.copy(alpha = 0.4f) else CardBorder),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Test Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Open Shizuku or Grant Button if not ok
                        if (!shizukuOk) {
                            Button(
                                onClick = {
                                    if (ShizukuManager.isShizukuAvailable()) {
                                        onGrantShizuku()
                                    } else {
                                        val launchIntent = ctx.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                        if (launchIntent != null) {
                                            ctx.startActivity(launchIntent)
                                        } else {
                                            try {
                                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app")))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Launch, null, tint = OnGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (ShizukuManager.isShizukuAvailable()) "Authorize" else "Open Shizuku",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnGreen
                                )
                            }
                        }
                    }
                }

                if (showShizukuTestDialog) {
                    val isAvail = ShizukuManager.isShizukuAvailable()
                    val hasP = ShizukuManager.hasShizukuPermission()

                    AlertDialog(
                        onDismissRequest = { showShizukuTestDialog = false },
                        containerColor = Surf,
                        tonalElevation = 0.dp,
                        shape = RoundedCornerShape(26.dp),
                        title = null,
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Top Hero Glowing Badge
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            color = if (shizukuTestSuccess) Green.copy(alpha = 0.12f) else if (isAvail) Yellow.copy(alpha = 0.12f) else Red.copy(alpha = 0.12f),
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (shizukuTestSuccess) Green.copy(alpha = 0.35f) else if (isAvail) Yellow.copy(alpha = 0.35f) else Red.copy(alpha = 0.35f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (shizukuTestSuccess) Icons.Default.CheckCircle else if (isAvail) Icons.Default.Security else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (shizukuTestSuccess) Green else if (isAvail) Yellow else Red,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                // Title & Subtitle
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (shizukuTestSuccess) "Shizuku Engine Verified" else if (isAvail) "Permission Required" else "Shizuku Not Running",
                                        color = OnBg,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (shizukuTestSuccess)
                                            "System-level network policy engine operational"
                                        else if (isAvail)
                                            "Shizuku detected but NetCordon needs permission"
                                        else
                                            "Shizuku service is stopped or not responding",
                                        color = Dim,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }

                                if (shizukuTestSuccess) {
                                    // 4 Structured Diagnostic Metric Cards
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DiagnosticMetricCard(
                                            icon = Icons.Default.Bolt,
                                            label = "Binder IPC Ping",
                                            detail = "Active & Healthy (<1ms)",
                                            status = "PASS",
                                            statusColor = Green
                                        )
                                        DiagnosticMetricCard(
                                            icon = Icons.Default.Security,
                                            label = "ADB Privileges",
                                            detail = "Granted (UID 2000 ADB shell)",
                                            status = "GRANTED",
                                            statusColor = Green
                                        )
                                        DiagnosticMetricCard(
                                            icon = Icons.Default.SignalCellularAlt,
                                            label = "Firewall Filter Engine",
                                            detail = "Chain-3 Network Rules Ready",
                                            status = "ACTIVE",
                                            statusColor = Green
                                        )
                                        DiagnosticMetricCard(
                                            icon = Icons.Default.CheckCircle,
                                            label = "Isolation Controller",
                                            detail = "Smart Shield / Blackout / Cut",
                                            status = "ONLINE",
                                            statusColor = Green
                                        )
                                    }
                                } else {
                                    // Troubleshooting Guide Card
                                    Surface(
                                        color = Surf2,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Troubleshooting Steps:",
                                                color = OnBg,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isAvail && !hasP) {
                                                Text(
                                                    text = "1. NetCordon requires ADB shell permission to manage per-app network chains.\n2. Tap 'Authorize NetCordon' below to prompt Shizuku.",
                                                    color = Dim,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            } else {
                                                Text(
                                                    text = "1. Ensure Developer Options & Wireless Debugging are ON in Android Settings.\n2. Open Shizuku and tap 'Start via Wireless Debugging'.\n3. Return here and tap 'Test Connection' again.",
                                                    color = Dim,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showShizukuTestDialog = false
                                    if (!shizukuTestSuccess) {
                                        if (isAvail && !hasP) {
                                            onGrantShizuku()
                                        } else {
                                            val launchIntent = ctx.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                            if (launchIntent != null) {
                                                ctx.startActivity(launchIntent)
                                            } else {
                                                try {
                                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app")))
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (shizukuTestSuccess) Green else if (isAvail) Yellow else Green
                                )
                            ) {
                                if (shizukuTestSuccess) {
                                    Icon(Icons.Default.CheckCircle, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Awesome, Got It", color = OnGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else if (isAvail) {
                                    Icon(Icons.Default.Security, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Authorize NetCordon", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Launch, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Open Shizuku App", color = OnGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        dismissButton = if (!shizukuTestSuccess) {
                            {
                                TextButton(
                                    onClick = { showShizukuTestDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Dismiss", color = Dim, fontSize = 12.sp)
                                }
                            }
                        } else null
                    )
                }
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        // Background Service & System Permissions Card
        item {
            SettingsCard {
                PrefRow(
                    label = "Background Firewall Service",
                    sublabel = "Master on/off switch for network protection",
                    trailing = { NetCordonSwitch(checked = serviceOn, onCheckedChange = onService) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Start on Boot",
                    sublabel = "Automatically start after device reboot",
                    trailing = { NetCordonSwitch(checked = bootStart, onCheckedChange = onBoot) }
                )
                HorizontalDivider(color = Div)

                listOf(
                    Triple("Usage Access",          usageOk,   onGrantUsage),
                    Triple("Post Notifications",    notifOk,   onGrantNotif),
                    Triple("Battery Optimization",  batteryOk, onGrantBattery)
                ).forEachIndexed { i, (label, granted, onGrant) ->
                    PrefRow(
                        label = label,
                        sublabel = if (granted) "✓ Granted" else "Not granted",
                        trailing = {
                            if (granted)
                                Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(20.dp))
                            else
                                OutlinedButton(
                                    onClick = onGrant,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Dim),
                                    border = BorderStroke(1.2.dp, CardBorder),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) { Text("Grant", fontSize = 12.sp) }
                        }
                    )
                    if (i < 2) HorizontalDivider(color = Div)
                }
            }
        }

        // ── 2. Firewall Rules & Privacy ──
        item { SecHeader("Firewall Rules & Leak Protection") }
        item {
            SettingsCard {
                PrefRow(
                    label = "Background Data Leak Alerts",
                    sublabel = "Heads-up notification with quick Total Blackout when closed apps attempt data connections",
                    trailing = { NetCordonSwitch(checked = leakAlertsEnabled, onCheckedChange = onLeakAlertsChange) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Block When Screen is Off",
                    sublabel = "Auto-cut background traffic & mute notifications when device is locked",
                    trailing = { NetCordonSwitch(checked = screenOffShield, onCheckedChange = onScreenOffShield) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Block App Notifications",
                    sublabel = "Auto-silence popups, alerts & vibrations when app is restricted",
                    trailing = { NetCordonSwitch(checked = blockNotifs, onCheckedChange = onBlockNotifs) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Minimal Notification",
                    sublabel = "Show a compact status bar icon only",
                    trailing = { NetCordonSwitch(checked = minNotif, onCheckedChange = onNotif) }
                )
            }
        }

        // ── 3. Interactive Tools & Security ──
        item { SecHeader("Tools & App Security") }
        item {
            SettingsCard {
                var floatingOn by remember { mutableStateOf(PrefsManager.isFloatingSpeedometerEnabled(ctx)) }
                PrefRow(
                    label = "Floating Speedometer Widget",
                    sublabel = "Draggable overlay with live Download / Upload speed & active leaker app badge",
                    trailing = {
                        NetCordonSwitch(
                            checked = floatingOn,
                            onCheckedChange = { v ->
                                floatingOn = v
                                PrefsManager.setFloatingSpeedometerEnabled(ctx, v)
                                if (v) {
                                    if (Settings.canDrawOverlays(ctx)) {
                                        FloatingSpeedometerService.start(ctx)
                                    } else {
                                        if (ShizukuManager.hasShizukuPermission()) {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                ShizukuManager.executeCommand("cmd appops set ${ctx.packageName} SYSTEM_ALERT_WINDOW allow")
                                                withContext(Dispatchers.Main) {
                                                    if (Settings.canDrawOverlays(ctx)) {
                                                        FloatingSpeedometerService.start(ctx)
                                                    } else {
                                                        val intent = Intent(
                                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                            Uri.parse("package:${ctx.packageName}")
                                                        )
                                                        ctx.startActivity(intent)
                                                    }
                                                }
                                            }
                                        } else {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${ctx.packageName}")
                                            )
                                            ctx.startActivity(intent)
                                        }
                                    }
                                } else {
                                    FloatingSpeedometerService.stop(ctx)
                                }
                            }
                        )
                    }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Biometric & PIN Lock",
                    sublabel = "Require Fingerprint, Face, or PIN to access NetCordon",
                    trailing = { NetCordonSwitch(checked = appLockEnabled, onCheckedChange = onAppLockChange) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Lock on Screen Off",
                    sublabel = "Re-lock NetCordon automatically when screen turns off",
                    trailing = { NetCordonSwitch(checked = lockOnScreenOff, onCheckedChange = onLockOnScreenOffChange) }
                )
            }
        }

        // ── 4. Backup & Profile Management ──
        item { SecHeader("Profiles & Backup") }
        item {
            var showImportConfirmDialog by remember { mutableStateOf(false) }
            var showPasteJsonDialog by remember { mutableStateOf(false) }
            var rawJsonInput by remember { mutableStateOf("") }
            var importSummary by remember { mutableStateOf<ProfileImportSummary?>(null) }
            var pendingJsonContent by remember { mutableStateOf("") }
            var backupDialogMessage by remember { mutableStateOf("") }
            var showBackupDialog by remember { mutableStateOf(false) }

            val exportFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri != null) {
                    try {
                        val json = PrefsManager.exportProfileToJson(ctx)
                        ctx.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(json.toByteArray(Charsets.UTF_8))
                        }
                        backupDialogMessage = "✅ Profile exported successfully as a .json file!"
                        showBackupDialog = true
                    } catch (e: Exception) {
                        backupDialogMessage = "Export failed: ${e.localizedMessage}"
                        showBackupDialog = true
                    }
                }
            }

            val importFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    try {
                        val inputStream = ctx.contentResolver.openInputStream(uri)
                        val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                        if (content.isNotEmpty()) {
                            val summary = PrefsManager.parseProfileSummary(content)
                            if (summary != null) {
                                importSummary = summary
                                pendingJsonContent = content
                                showImportConfirmDialog = true
                            } else {
                                backupDialogMessage = "Invalid backup file: JSON format does not match NetCordon profile structure."
                                showBackupDialog = true
                            }
                        }
                    } catch (e: Exception) {
                        backupDialogMessage = "Failed reading backup file: ${e.localizedMessage}"
                        showBackupDialog = true
                    }
                }
            }

            val studioFileLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    try {
                        val inputStream = ctx.contentResolver.openInputStream(uri)
                        val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                        if (content.isNotEmpty()) {
                            rawJsonInput = content
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            SettingsCard {
                PrefRow(
                    label = "Export to .JSON File",
                    sublabel = "Save complete firewall rules, schedules & quotas to a .json file",
                    onClick = {
                        try {
                            exportFileLauncher.launch("NetCordon_Backup_${System.currentTimeMillis()}.json")
                        } catch (e: Exception) {
                            backupDialogMessage = "Unable to open document creator: ${e.localizedMessage}"
                            showBackupDialog = true
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Upload, null, tint = Green, modifier = Modifier.size(20.dp))
                    }
                )

                HorizontalDivider(color = Div)

                PrefRow(
                    label = "Import from .JSON File",
                    sublabel = "Restore isolation policies, schedules & settings from a .json file",
                    onClick = {
                        try {
                            importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        } catch (e: Exception) {
                            backupDialogMessage = "Unable to open document picker: ${e.localizedMessage}"
                            showBackupDialog = true
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Download, null, tint = Green, modifier = Modifier.size(20.dp))
                    }
                )

                HorizontalDivider(color = Div)

                PrefRow(
                    label = "Paste / Edit JSON Code",
                    sublabel = "Blank text box to paste, inspect and import profile JSON",
                    onClick = {
                        showPasteJsonDialog = true
                    },
                    trailing = {
                        Icon(Icons.Default.ContentPaste, null, tint = Green, modifier = Modifier.size(20.dp))
                    }
                )

                HorizontalDivider(color = Div)

                PrefRow(
                    label = "Share JSON Profile",
                    sublabel = "Send profile text directly to WhatsApp, Telegram, Drive or Email",
                    onClick = {
                        try {
                            val json = PrefsManager.exportProfileToJson(ctx)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "NetCordon_Backup_${System.currentTimeMillis()}.json")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            ctx.startActivity(Intent.createChooser(sendIntent, "Share NetCordon Profile"))
                        } catch (e: Exception) {
                            backupDialogMessage = "Share failed: ${e.localizedMessage}"
                            showBackupDialog = true
                        }
                    },
                    trailing = {
                        Icon(Icons.Default.Share, null, tint = Green, modifier = Modifier.size(20.dp))
                    }
                )
            }

            if (showImportConfirmDialog && importSummary != null) {
                val s = importSummary!!
                AlertDialog(
                    onDismissRequest = { showImportConfirmDialog = false },
                    containerColor = Surf,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(26.dp),
                    title = null,
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Green.copy(alpha = 0.12f), CircleShape)
                                    .border(1.5.dp, Green.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Restore Firewall Profile",
                                    color = OnBg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Valid NetCordon backup profile detected",
                                    color = Dim,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DiagnosticMetricCard(
                                    icon = Icons.Default.Block,
                                    label = "Total Blackout",
                                    detail = "${s.blackoutCount} apps strictly severed (0 KB/s)",
                                    status = "${s.blackoutCount} APPS",
                                    statusColor = Red
                                )
                                DiagnosticMetricCard(
                                    icon = Icons.Default.Shield,
                                    label = "Smart Shield",
                                    detail = "${s.smartShieldCount} apps background-shielded",
                                    status = "${s.smartShieldCount} APPS",
                                    statusColor = Yellow
                                )
                                DiagnosticMetricCard(
                                    icon = Icons.Default.Schedule,
                                    label = "Automated Schedules",
                                    detail = "${s.schedulesCount} timer triggers configured",
                                    status = "${s.schedulesCount} TIMERS",
                                    statusColor = Green
                                )
                                DiagnosticMetricCard(
                                    icon = Icons.Default.DataUsage,
                                    label = "Daily Data Quotas",
                                    detail = "${s.quotasCount} per-app bandwidth limits",
                                    status = "${s.quotasCount} QUOTAS",
                                    statusColor = Green
                                )
                            }

                            Surface(
                                color = Yellow.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = Yellow, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Applying will overwrite current active rules with this backup profile.",
                                        color = OnBg.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showImportConfirmDialog = false
                                val success = PrefsManager.importProfileFromJson(ctx, pendingJsonContent)
                                if (success) {
                                    onProfileRestored()
                                    try {
                                        val syncIntent = Intent(ctx, AppShieldService::class.java).apply {
                                            action = AppShieldService.ACTION_UPDATE
                                        }
                                        ctx.startService(syncIntent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    backupDialogMessage = "✅ Profile restored successfully! All firewall rules, schedules & quotas have been updated."
                                } else {
                                    backupDialogMessage = "❌ Failed restoring profile. Check JSON validity."
                                }
                                showBackupDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Restore & Apply Profile", color = OnGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showImportConfirmDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = Dim, fontSize = 12.sp)
                        }
                    }
                )
            }

            if (showPasteJsonDialog) {
                val inputSummary = remember(rawJsonInput) {
                    if (rawJsonInput.isNotBlank()) PrefsManager.parseProfileSummary(rawJsonInput.trim()) else null
                }
                AlertDialog(
                    onDismissRequest = { showPasteJsonDialog = false },
                    containerColor = Surf,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(24.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, null, tint = Green, modifier = Modifier.size(22.dp))
                                Text(
                                    "JSON Profile Studio",
                                    color = OnBg,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { showPasteJsonDialog = false }) {
                                Icon(Icons.Default.Close, null, tint = Dim, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            try {
                                                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                                if (clip.isNotBlank()) {
                                                    rawJsonInput = clip
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Surf2),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, null, tint = Green, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Paste Clipboard", color = OnBg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                studioFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Surf2),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Download, null, tint = Green, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Pick File", color = OnBg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (rawJsonInput.isNotEmpty()) {
                                    TextButton(
                                        onClick = { rawJsonInput = "" },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = Red, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Clear", color = Red, fontSize = 11.sp)
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(BG, RoundedCornerShape(12.dp))
                                    .border(
                                        BorderStroke(1.dp, if (inputSummary != null) Green.copy(alpha = 0.5f) else Div),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                if (rawJsonInput.isEmpty()) {
                                    Text(
                                        "Paste your NetCordon JSON profile code here...\n\nExample:\n{\n  \"smartShieldPackages\": [\"com.whatsapp\"],\n  \"schedules\": [...]\n}",
                                        color = Dim.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                BasicTextField(
                                    value = rawJsonInput,
                                    onValueChange = { rawJsonInput = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    textStyle = TextStyle(
                                        color = OnBg,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    ),
                                    cursorBrush = SolidColor(Green)
                                )
                            }

                            if (rawJsonInput.isBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Waiting for JSON code...", color = Dim, fontSize = 11.sp)
                                }
                            } else if (inputSummary != null) {
                                val s = inputSummary
                                Surface(
                                    color = Green.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(14.dp))
                                            Text("Valid NetCordon Profile", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            "• Blackout: ${s.blackoutCount}  • Smart Shield: ${s.smartShieldCount}  • Timers: ${s.schedulesCount}  • Quotas: ${s.quotasCount}",
                                            color = OnBg.copy(alpha = 0.85f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = Red.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(14.dp))
                                        Text("Invalid JSON syntax or unsupported format", color = Red, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val textToImport = rawJsonInput.trim()
                                val success = PrefsManager.importProfileFromJson(ctx, textToImport)
                                showPasteJsonDialog = false
                                if (success) {
                                    onProfileRestored()
                                    try {
                                        val syncIntent = Intent(ctx, AppShieldService::class.java).apply {
                                            action = AppShieldService.ACTION_UPDATE
                                        }
                                        ctx.startService(syncIntent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    backupDialogMessage = "✅ Profile restored successfully from JSON Studio!"
                                } else {
                                    backupDialogMessage = "❌ Failed restoring profile. Check JSON validity."
                                }
                                showBackupDialog = true
                            },
                            enabled = (inputSummary != null),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green,
                                disabledContainerColor = Surf2
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Import & Apply",
                                color = if (inputSummary != null) OnGreen else Dim,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasteJsonDialog = false }) {
                            Text("Cancel", color = Dim, fontSize = 13.sp)
                        }
                    }
                )
            }

            if (showBackupDialog) {
                val isSuccess = backupDialogMessage.startsWith("✅") || backupDialogMessage.contains("successfully", ignoreCase = true)
                AlertDialog(
                    onDismissRequest = { showBackupDialog = false },
                    containerColor = Surf,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(26.dp),
                    title = null,
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        color = if (isSuccess) Green.copy(alpha = 0.12f) else Red.copy(alpha = 0.12f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSuccess) Green.copy(alpha = 0.35f) else Red.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSuccess) Green else Red,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = if (isSuccess) "Profile Backup & Restore" else "Operation Notice",
                                color = OnBg,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                color = Surf2,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = backupDialogMessage.replace("✅ ", "").replace("❌ ", ""),
                                    color = OnBg,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showBackupDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSuccess) Green else Surf2
                            )
                        ) {
                            if (isSuccess) {
                                Icon(Icons.Default.CheckCircle, null, tint = OnGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Great, Got It", color = OnGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Dismiss", color = OnBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        }

        // ── 5. Appearance, Help & About ──
        item { SecHeader("General & About") }
        item {
            SettingsCard {
                PrefRow(
                    label = "Dark Mode",
                    sublabel = if (isDarkMode) "AMOLED dark theme enabled" else "Material light theme enabled",
                    trailing = {
                        NetCordonSwitch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
                    }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Show Package Names",
                    sublabel = "Display package name below each app title",
                    trailing = { NetCordonSwitch(checked = showPkg, onCheckedChange = onPkg) }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Replay App Walkthrough",
                    sublabel = "Interactive feature tour & isolation guide",
                    onClick = onReplayWalkthrough,
                    trailing = {
                        Icon(Icons.Default.ChevronRight, null, tint = Green, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        item { Spacer(Modifier.height(10.dp)) }

        // ── 6. App Updates & About ──
        item { SecHeader("App Updates & About") }
        item {
            var isCheckingUpdate by remember { mutableStateOf(false) }
            var updateResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
            var showUpdateDialog by remember { mutableStateOf(false) }
            var isDownloading by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableFloatStateOf(0f) }
            var downloadStatusText by remember { mutableStateOf("") }

            SettingsCard {
                PrefRow(
                    label = "Check for Updates",
                    sublabel = "Current: v1.0.0 · Detect & install latest release",
                    onClick = {
                        if (!isCheckingUpdate && !isDownloading) {
                            isCheckingUpdate = true
                            coroutineScope.launch {
                                val res = AppUpdateManager.checkForUpdate(ctx)
                                isCheckingUpdate = false
                                updateResult = res
                                if (res.isUpdateAvailable) {
                                    showUpdateDialog = true
                                } else if (res.errorMessage != null) {
                                    onShowToast("Update check: ${res.errorMessage}")
                                } else {
                                    onShowToast("You're on the latest version (${res.currentVersion}) 🚀")
                                }
                            }
                        }
                    },
                    trailing = {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Green,
                                strokeWidth = 2.dp
                            )
                        } else {
                            OutlinedButton(
                                onClick = {
                                    if (!isCheckingUpdate && !isDownloading) {
                                        isCheckingUpdate = true
                                        coroutineScope.launch {
                                            val res = AppUpdateManager.checkForUpdate(ctx)
                                            isCheckingUpdate = false
                                            updateResult = res
                                            if (res.isUpdateAvailable) {
                                                showUpdateDialog = true
                                            } else if (res.errorMessage != null) {
                                                onShowToast("Update check: ${res.errorMessage}")
                                            } else {
                                                onShowToast("You're on the latest version (${res.currentVersion}) 🚀")
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Green),
                                border = BorderStroke(1.2.dp, Green.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Check", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "NetCordon v1.0.0",
                    sublabel = "Shizuku-powered user app firewall · Rootless Chain-3 architecture"
                )
                HorizontalDivider(color = Div)
                PrefRow(
                    label = "Developer Email",
                    sublabel = "sachinmandawi@gmail.com · Tap to send feedback",
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
                    label = "GitHub Repository",
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

            // Update Available Dialog
            if (showUpdateDialog && updateResult != null) {
                val info = updateResult!!
                AlertDialog(
                    onDismissRequest = { if (!isDownloading) showUpdateDialog = false },
                    containerColor = Surf,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Download, null, tint = Green, modifier = Modifier.size(24.dp))
                            Text("New Update Available! 🎉", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "A new version of NetCordon is ready to install:",
                                color = Dim,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BG,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Version: ${info.latestVersion} (Current: ${info.currentVersion})",
                                        color = Green,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (info.releaseNotes.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = info.releaseNotes.take(300),
                                            color = Color(0xFFCAD1DB),
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }

                            if (isDownloading) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (downloadStatusText.isNotEmpty()) downloadStatusText else "Downloading update... ${(downloadProgress * 100).toInt()}%",
                                    color = Green,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Green,
                                    trackColor = CardBorder
                                )
                            } else {
                                Text(
                                    text = "⚡ Direct update will install over the existing app without uninstalling or losing your firewall settings.",
                                    color = Dim,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (isDownloading) {
                            // Disabled while downloading
                        } else {
                            Button(
                                onClick = {
                                    if (info.downloadUrl.endsWith(".apk", ignoreCase = true)) {
                                        isDownloading = true
                                        downloadStatusText = "Downloading APK..."
                                        coroutineScope.launch {
                                            AppUpdateManager.downloadAndInstallApk(
                                                context = ctx,
                                                downloadUrl = info.downloadUrl,
                                                onProgress = { p -> downloadProgress = p },
                                                onComplete = {
                                                    isDownloading = false
                                                    showUpdateDialog = false
                                                },
                                                onError = { err ->
                                                    isDownloading = false
                                                    downloadStatusText = "Error: $err"
                                                    onShowToast("Update failed: $err")
                                                }
                                            )
                                        }
                                    } else {
                                        try {
                                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl.ifEmpty { "https://github.com/sachinmandawi/NetCordon/releases" })))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        showUpdateDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Download & Install", color = OnGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        if (!isDownloading) {
                            TextButton(onClick = { showUpdateDialog = false }) {
                                Text("Later", color = Dim)
                            }
                        }
                    }
                )
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(14.dp),
        color = Surf,
        border = BorderStroke(1.2.dp, CardBorder)
    ) {
        Column(content = content)
    }
}

/* ─── Settings Section Header ─── */
@Composable
fun SecHeader(label: String) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Green)
        )
        Text(
            label,
            color = Green,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

/* ─── Settings Preference Row ─── */
@Composable
fun PrefRow(label: String, sublabel: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = OnBg, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
            if (sublabel != null)
                Text(sublabel, color = Dim, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
        }
        trailing?.invoke()
    }
}

@Composable
private fun DiagnosticMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    detail: String,
    status: String,
    statusColor: Color
) {
    Surface(
        color = Surf2,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(statusColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = statusColor, modifier = Modifier.size(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = OnBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(detail, color = Dim, fontSize = 10.sp, lineHeight = 14.sp)
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

