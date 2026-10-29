package com.goivlab.app

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * On-device diagnostics for a manually started overlay.
 * Does not read Pokémon GO, screenshots, clipboard, account or location data.
 */
internal object OverlayDiagnostics {
    private const val PREFS = "go_iv_lab_overlay_diagnostics"
    private const val LAST_EVENT = "last_event"
    private const val LAST_EVENT_TIME = "last_event_time"
    private const val LAST_ERROR = "last_error"

    fun record(context: Context, event: String, errorClass: String? = null) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(LAST_EVENT, event.take(100))
            .putLong(LAST_EVENT_TIME, System.currentTimeMillis())
        if (errorClass != null) {
            editor.putString(LAST_ERROR, errorClass.take(100))
        } else if (event == "Overlay foreground notification active") {
            editor.remove(LAST_ERROR)
        }
        editor.apply()
    }

    fun report(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val whenRecorded = prefs.getLong(LAST_EVENT_TIME, 0L)
        val eventTime = if (whenRecorded > 0L) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(whenRecorded))
        } else "never"
        val permission = if (Settings.canDrawOverlays(context)) "granted" else "not granted"
        return listOf(
            "GO IV Lab Overlay Lab — local diagnostic",
            "Package: ${context.packageName}",
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}",
            "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Draw over other apps: $permission",
            "Last service event: ${prefs.getString(LAST_EVENT, "none")}",
            "Last event time: $eventTime",
            "Last service error class: ${prefs.getString(LAST_ERROR, "none")}",
            "Note: last event is historical and does not prove an overlay is currently visible.",
            "No screenshots, game account information, location, or clipboard contents included."
        ).joinToString("\n")
    }
}
