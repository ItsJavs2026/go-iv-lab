package com.goivlab.app

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * On-device diagnostics for a manually started overlay.
 * Fixed internal lifecycle events only; no game data, screenshots, account or location.
 */
internal object OverlayDiagnostics {
    private const val PREFS = "go_iv_lab_overlay_diagnostics"
    private const val LAST_EVENT = "last_event"
    private const val LAST_EVENT_TIME = "last_event_time"
    private const val LAST_ERROR = "last_error"
    private const val HISTORY = "recent_events"

    fun record(context: Context, event: String, errorClass: String? = null) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(now))
        val safeEvent = event.replace("\n", " ").replace("\r", " ").take(100)
        val safeError = errorClass?.replace("\n", " ")?.replace("\r", " ")?.take(100)
        val entry = "$stamp | $safeEvent" +
            (if (safeError == null) "" else " | $safeError")
        val history = (prefs.getString(HISTORY, "").orEmpty().lines().filter { it.isNotBlank() } +
            entry).takeLast(12).joinToString("\n")
        val editor = prefs.edit()
            .putString(LAST_EVENT, safeEvent)
            .putLong(LAST_EVENT_TIME, now)
            .putString(HISTORY, history)
        if (safeError != null) {
            editor.putString(LAST_ERROR, safeError)
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
            "Recent lifecycle events (up to 12, oldest first):",
            prefs.getString(HISTORY, "none").orEmpty(),
            "Note: recorded events do not prove an overlay is currently visible over Pokémon GO.",
            "No screenshots, game account information, location, or clipboard contents included."
        ).joinToString("\n")
    }
}
