package com.goivlab.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

/**
 * A user-started floating card for already-calculated offline results.
 * No screen recording, accessibility, game automation, or network traffic.
 * A foreground notification allows the user to stop the session outside the app.
 */
class ManualOverlayService : Service() {
    companion object {
        const val ACTION_SHOW = "com.goivlab.app.overlay.SHOW"
        const val ACTION_STOP = "com.goivlab.app.overlay.STOP"
        const val EXTRA_RESULT = "result"
        private const val CHANNEL_ID = "go_iv_lab_overlay_v04"
        private const val NOTIFICATION_ID = 1404
        private const val PREF_NAME = "go_iv_lab_overlay_state"
    }

    private var windowManager: WindowManager? = null
    private var overlay: LinearLayout? = null
    private var layout: WindowManager.LayoutParams? = null
    private var details: TextView? = null
    private var collapsed = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action != ACTION_SHOW || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val content = intent.getStringExtra(EXTRA_RESULT)?.trim()?.take(350)
        if (content.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            if (overlay == null) createOverlay(content) else details?.text = content
            // Started from a visible Activity; do not attempt background auto-restarts.
            startPersistentNotification()
        } catch (_: Exception) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun createOverlay(content: String) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager = wm
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        collapsed = prefs.getBoolean("collapsed", false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(7))
            background = GradientDrawable().apply {
                setColor(Color.rgb(27, 38, 60))
                cornerRadius = dp(14).toFloat()
            }
            elevation = dp(8).toFloat()
            contentDescription = "GO IV Lab floating rank result"
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dragHandle = TextView(this).apply {
            text = if (collapsed) "GO IV" else "GO IV  •  drag"
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(6), 0, dp(6))
            contentDescription = "Hold and drag to move floating result"
        }
        header.addView(dragHandle, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        val collapseControl = TextView(this).apply {
            text = if (collapsed) "+" else "−"
            textSize = 21f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            contentDescription = if (collapsed) "Expand floating result" else "Minimize floating result"
        }
        header.addView(collapseControl, LinearLayout.LayoutParams(dp(44), dp(44)))
        val close = TextView(this).apply {
            text = "×"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            contentDescription = "Close floating result"
            setOnClickListener { stopSelf() }
        }
        header.addView(close, LinearLayout.LayoutParams(dp(35), dp(38)))
        root.addView(header)
        val body = TextView(this).apply {
            text = content
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(6), dp(4), dp(5), dp(6))
            setLineSpacing(dp(2).toFloat(), 1f)
            visibility = if (collapsed) View.GONE else View.VISIBLE
        }
        root.addView(body)
        details = body

        val params = WindowManager.LayoutParams(
            preferredWidth(wm),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("x", dp(12))
            y = prefs.getInt("y", dp(85))
        }
        layout = params
        val bounds = wm.currentWindowMetrics.bounds
        params.x = params.x.coerceIn(0, (bounds.width() - params.width).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (bounds.height() - dp(70)).coerceAtLeast(0))

        collapseControl.setOnClickListener {
            collapsed = !collapsed
            dragHandle.text = if (collapsed) "GO IV" else "GO IV  •  drag"
            body.visibility = if (collapsed) View.GONE else View.VISIBLE
            collapseControl.text = if (collapsed) "+" else "−"
            collapseControl.contentDescription = if (collapsed) "Expand floating result" else "Minimize floating result"
            params.width = preferredWidth(wm)
            if (!updatePosition()) return@setOnClickListener
            prefs.edit().putBoolean("collapsed", collapsed).apply()
        }

        var downX = 0f
        var downY = 0f
        var baseX = 0
        var baseY = 0
        dragHandle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    baseX = params.x
                    baseY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (baseX + event.rawX - downX).roundToInt()
                    params.y = (baseY + event.rawY - downY).roundToInt()
                    updatePosition()
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit().putInt("x", params.x).putInt("y", params.y).apply()
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
        wm.addView(root, params)
        overlay = root
    }

    private fun preferredWidth(wm: WindowManager): Int =
        minOf(dp(if (collapsed) 163 else 270), (wm.currentWindowMetrics.bounds.width() - dp(12)).coerceAtLeast(dp(100)))

    private fun updatePosition(): Boolean {
        val params = layout ?: return false
        val wm = windowManager ?: return false
        val view = overlay ?: return false
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return false
        }
        val bounds = wm.currentWindowMetrics.bounds
        params.x = params.x.coerceIn(0, (bounds.width() - params.width).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (bounds.height() - view.height).coerceAtLeast(0))
        return try {
            wm.updateViewLayout(view, params)
            true
        } catch (_: Exception) {
            stopSelf()
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        layout?.width = windowManager?.let { preferredWidth(it) } ?: return
        updatePosition()
    }

    private fun startPersistentNotification() {
        val notifications = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "GO IV Lab floating result", NotificationManager.IMPORTANCE_LOW)
        )
        val stopIntent = Intent(this, ManualOverlayService::class.java).apply { action = ACTION_STOP }
        val stopAction = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("GO IV Lab overlay active")
            .setContentText("Tap to open GO IV Lab, or stop the floating card.")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel, "Stop overlay", stopAction
            ).build())
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun removeOverlay() {
        val view = overlay
        overlay = null
        details = null
        layout = null
        if (view != null) {
            try { windowManager?.removeViewImmediate(view) } catch (_: Exception) {}
        }
        windowManager = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
