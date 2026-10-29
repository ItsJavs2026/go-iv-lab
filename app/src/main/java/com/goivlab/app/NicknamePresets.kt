package com.goivlab.app

import android.content.SharedPreferences
import org.json.JSONArray

/** On-device nickname templates. No account or network permissions. */
internal class NicknamePresets(private val prefs: SharedPreferences) {
    companion object {
        private const val KEY = "nickname_presets_v03"
        private const val MAX_PRESETS = 12
        private const val MAX_TEMPLATE_LENGTH = 120
        private val DEFAULTS = listOf("{league}{rank}", "{name}", "{attack}/{defense}/{hp}")
    }

    fun all(): List<String> {
        val raw = prefs.getString(KEY, null) ?: return DEFAULTS
        return try {
            val data = JSONArray(raw)
            (0 until data.length()).map { data.getString(it) }
                .filter { it.isNotBlank() && it.length <= MAX_TEMPLATE_LENGTH }
                .distinct().take(MAX_PRESETS)
        } catch (_: Exception) {
            DEFAULTS
        }
    }

    fun save(template: String): Boolean {
        val clean = template.trim()
        if (clean.isEmpty() || clean.length > MAX_TEMPLATE_LENGTH) return false
        val values = (listOf(clean) + all().filterNot { it == clean }).take(MAX_PRESETS)
        return persist(values)
    }

    fun remove(template: String): Boolean {
        if (template !in all()) return false
        return persist(all().filterNot { it == template })
    }

    private fun persist(values: List<String>): Boolean =
        prefs.edit().putString(KEY, JSONArray(values).toString()).commit()
}
