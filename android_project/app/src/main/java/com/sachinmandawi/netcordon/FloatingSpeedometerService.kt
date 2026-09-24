package com.sachinmandawi.netcordon

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class FloatingSpeedometerService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private lateinit var speedTextView: TextView
    private lateinit var leakerBadgeView: TextView
    private lateinit var containerLayout: LinearLayout
    private val appNameCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    private var leakerRefreshTicks = 0

    private fun getAppName(packageName: String): String {
        return appNameCache.getOrPut(packageName) {
            try {
                val pm = packageManager
                val ai = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getApplicationInfo(packageName, android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(packageName, 0)
                }
                val label = pm.getApplicationLabel(ai).toString()
                if (label.length > 12) label.take(11) + "…" else label
            } catch (e: Exception) {
                packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            updateSpeed()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initFloatingView()
        startSpeedMonitoring()
    }

    private fun initFloatingView() {
        val density = resources.displayMetrics.density
        val dpToPx = { dp: Int -> (dp * density).toInt() }

        // Root Container
        containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            
            // AMOLED dark background with sleek neon border
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#E60E1310")) // 90% opacity dark slate
                cornerRadius = dpToPx(18).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#2E3E34"))
            }
            background = bg
        }

        // Live dot indicator
        val dotView = View(this).apply {
            val dotBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#00E676")) // Neon Green
            }
            background = dotBg
            val dotParams = LinearLayout.LayoutParams(dpToPx(7), dpToPx(7)).apply {
                marginEnd = dpToPx(8)
            }
            layoutParams = dotParams
        }
        containerLayout.addView(dotView)

        // Speed Text View (Download / Upload)
        speedTextView = TextView(this).apply {
            text = "↓ 0 B/s  ↑ 0 B/s"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        containerLayout.addView(speedTextView)

        // Leaker Badge / Separator (Shows top blocked app or shield active)
        leakerBadgeView = TextView(this).apply {
            text = "SHIELD"
            setTextColor(Color.parseColor("#00E676"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
            
            val badgeBg = GradientDrawable().apply {
                setColor(Color.parseColor("#2600E676"))
                cornerRadius = dpToPx(6).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#4D00E676"))
            }
            background = badgeBg
            val badgeParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(8)
            }
            layoutParams = badgeParams
        }
        containerLayout.addView(leakerBadgeView)

        // Window Layout Params
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(20)
            y = dpToPx(120)
        }

        // Draggable touch listener
        setupDragTouchListener()

        try {
            windowManager?.addView(containerLayout, layoutParams)
            floatingView = containerLayout
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun setupDragTouchListener() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        containerLayout.setOnTouchListener { _, event ->
            val params = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    try {
                        windowManager?.updateViewLayout(containerLayout, params)
                    } catch (e: Exception) {
                        // View may be detached during rapid transition
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startSpeedMonitoring() {
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTimestamp = System.currentTimeMillis()
        isRunning = true
        handler.post(updateRunnable)
    }

    private fun updateSpeed() {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTimestamp = System.currentTimeMillis()

        val timeDiff = (currentTimestamp - lastTimestamp) / 1000f
        if (timeDiff <= 0f) return

        val rxDiff = if (currentRx >= lastRxBytes) currentRx - lastRxBytes else 0L
        val txDiff = if (currentTx >= lastTxBytes) currentTx - lastTxBytes else 0L

        val rxSpeed = (rxDiff / timeDiff).toLong()
        val txSpeed = (txDiff / timeDiff).toLong()

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimestamp = currentTimestamp

        speedTextView.text = "↓ ${formatSpeed(rxSpeed)}  ↑ ${formatSpeed(txSpeed)}"

        if (leakerRefreshTicks % 5 == 0) {
            val topLeakers = PrefsManager.getTodayBlockedAttempts(this)
            if (topLeakers.isNotEmpty()) {
                val topApp = topLeakers.maxByOrNull { it.value }
                if (topApp != null && topApp.value > 0) {
                    val appLabel = getAppName(topApp.key)
                    leakerBadgeView.text = "🚨 $appLabel (${topApp.value})"
                    leakerBadgeView.setTextColor(Color.parseColor("#FF5252"))
                } else {
                    leakerBadgeView.text = "🛡️ SHIELD"
                    leakerBadgeView.setTextColor(Color.parseColor("#00E676"))
                }
            } else {
                leakerBadgeView.text = "🛡️ SHIELD"
                leakerBadgeView.setTextColor(Color.parseColor("#00E676"))
            }
        }
        leakerRefreshTicks++
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> "${bytesPerSec / 1024} KB/s"
            else -> String.format(java.util.Locale.US, "%.1f MB/s", bytesPerSec / (1024f * 1024f))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            floatingView = null
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FloatingSpeedometerService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingSpeedometerService::class.java)
            context.stopService(intent)
        }
    }
}
