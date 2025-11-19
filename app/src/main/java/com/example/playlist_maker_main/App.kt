package com.example.playlist_maker_main

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlist_maker_main.creator.Creator
import com.example.playlist_maker_main.settings.domain.ThemeInteractor

class App : Application() {

    lateinit var themeInteractor: ThemeInteractor
        private set

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        themeInteractor = Creator.provideThemeInteractor(this)

        darkTheme = if (themeInteractor.isThemeSet()) {
            themeInteractor.isDarkThemeEnabled()
        } else {
            val systemDark = isSystemDark(this)
            themeInteractor.setDarkThemeEnabled(systemDark)
            systemDark
        }

        applyNightMode(darkTheme)
    }

    fun switchTheme(enableDark: Boolean) {
        if (darkTheme == enableDark) return
        darkTheme = enableDark
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
