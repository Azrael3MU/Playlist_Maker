package com.example.playlist_maker_main

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlist_maker_main.di.dataModule
import com.example.playlist_maker_main.di.domainModule
import com.example.playlist_maker_main.di.presentationModule
import com.example.playlist_maker_main.settings.domain.ThemeInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.android.inject
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.logger.Level

class App : Application(), KoinComponent {

    private val themeInteractor: ThemeInteractor by inject()

    var darkTheme: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@App)
            modules(listOf(dataModule, domainModule, presentationModule))
        }

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
