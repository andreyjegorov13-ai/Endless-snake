package com.example.endlesssnake.data

import android.content.Context
import android.content.SharedPreferences

enum class Difficulty { EASY, NORMAL, HARD }

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("snake_settings", Context.MODE_PRIVATE)

    var soundVolume: Int
        get() = prefs.getInt("sound_volume", 80)
        set(value) { prefs.edit().putInt("sound_volume", value.coerceIn(0, 100)).apply() }

    var musicVolume: Int
        get() = prefs.getInt("music_volume", 60)
        set(value) { prefs.edit().putInt("music_volume", value.coerceIn(0, 100)).apply() }

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) { prefs.edit().putBoolean("sound_enabled", value).apply() }

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) { prefs.edit().putBoolean("music_enabled", value).apply() }

    var useButtons: Boolean
        get() = prefs.getBoolean("use_buttons", false)
        set(value) { prefs.edit().putBoolean("use_buttons", value).apply() }

    var difficulty: Difficulty
        get() = try { Difficulty.valueOf(prefs.getString("difficulty", "NORMAL") ?: "NORMAL") } catch (e: Exception) { Difficulty.NORMAL }
        set(value) { prefs.edit().putString("difficulty", value.name).apply() }

    var bestScore: Int
        get() = prefs.getInt("best_score", 0)
        set(value) { prefs.edit().putInt("best_score", value).apply() }

    var language: String
        get() = prefs.getString("language", "ru") ?: "ru"
        set(value) { prefs.edit().putString("language", value).apply() }
}