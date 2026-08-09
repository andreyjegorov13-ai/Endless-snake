package com.example.endlesssnake.util

import android.app.Activity
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun apply(activity: Activity, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
}