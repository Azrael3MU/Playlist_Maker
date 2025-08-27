package com.example.playlist_maker_main

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    private lateinit var prefs: SharedPreferences

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val saved = prefs.contains(KEY_DARK_THEME)
        darkTheme = if (saved) {
            prefs.getBoolean(KEY_DARK_THEME, false)
        } else {
            isSystemDark(this)
        }

        applyNightMode(darkTheme)
    }

    fun switchTheme(enableDark: Boolean) {
        if (darkTheme == enableDark) return
        darkTheme = enableDark
        prefs.edit().putBoolean(KEY_DARK_THEME, darkTheme).apply()
        applyNightMode(darkTheme)
    }

    private fun applyNightMode(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

fun isSystemDark(context: Context): Boolean {
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}