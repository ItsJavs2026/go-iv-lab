package com.goivlab.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import kotlin.math.roundToInt

/**
 * Experimental manual-result overlay. No screen capture, OCR, accessibility or game input.
 * This is a short-lived started service, not a guaranteed persistent background overlay.
 */
class ManualOverlayService : Service() {
    companion object {
        const val ACTION_SHOW = "com.goivlab.app.overlay.SHOW"
        const val ACTION_STOP = "com.goivlab.app.overlay.STOP"
        const val EXTRA_RESULT = "result"
    }

    private var manager: WindowManager? = null
    private var overlay: View? = null

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
        val result = intent.getStringExtra(EXTRA_RESULT)?.take(350) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        show(result)
        return START_NOT_STICKY
    }

    private fun show(result: String) {
        removeOverlay()
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        manager = wm
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                setColor(Color.rgb(25, 34, 52))
                cornerRadius = dp(14).toFloat()
            }
            elevation = dp(6).toFloat()
        }
        val header = TextView(this).apply {
            text = "GO IV LAB  ·  drag to move"
            setTextColor(Color.WHITE)
            textSize = 13f
        }
        val details = TextView(this).apply {
            text = result
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(0, dp(5), 0, dp(5))
        }
        val close = Button(this).apply {
            text = "CLOSE OVERLAY"
            setOnClickListener { stopSelf() }
        }
        card.addView(header)
        card.addView(details)
        card.addView(close)
        val params = WindowManager.LayoutParams(
            dp(260), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(100)
        }
        var downX = 0f
        var downY = 0f
        var originX = 0
        var originY = 0
        header.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    originX = params.x
                    originY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = (originX + event.rawX - downX).roundToInt().coerceAtLeast(0)
                    params.y = (originY + event.rawY - downY).roundToInt().coerceAtLeast(0)
                    try { wm.updateViewLayout(card, params) } catch (_: IllegalArgumentException) {}
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
        try {
            wm.addView(card, params)
            overlay = card
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun removeOverlay() {
        val view = overlay ?: return
        overlay = null
        try { manager?.removeView(view) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        removeOverlay()
        manager = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
