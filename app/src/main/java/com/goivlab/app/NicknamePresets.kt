package com.goivlab.app

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** Locally stored templates; no network, cloud sync or external storage permission. */
internal class NicknamePresets(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY = "nickname_presets_v03"
        private const val MAX_PRESETS = 12
        private const val MAX_TEMPLATE_LENGTH = 120
    }

    fun all(): List<String> {
        val raw = prefs.getString(KEY, null) ?: return listOf(
            "{league}{rank}",
            "{name}",
            "{attack}/{defense}/{hp}"
        )
        return try {
            val data = JSONArray(raw)
            (0 until data.length()).map { data.getString(it) }
                .filter { it.isNotBlank() && it.length <= MAX_TEMPLATE_LENGTH }
                .distinct().take(MAX_PRESETS)
        } catch (_: Exception) {
            listOf("{league}{rank}")
        }
    }

    fun save(template: String): Boolean {
        val clean = template.trim()
        if (clean.isEmpty() || clean.length > MAX_TEMPLATE_LENGTH) return false
        val values = (listOf(clean) + all().filterNot { it == clean }).take(MAX_PRESETS)
        return prefs.edit().putString(KEY, JSONArray(values).toString()).commit()
    }

    fun remove(template: String): Boolean {
        val values = all().filterNot { it == template }
        return prefs.edit().putString(KEY, JSONArray(values).toString()).commit()
    }
}
