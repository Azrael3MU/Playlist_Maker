package com.example.playlist_maker_main.settings.data

import android.content.SharedPreferences
import com.example.playlist_maker_main.settings.domain.repository.ThemeRepository

class ThemeRepositoryImpl(
    private val prefs: SharedPreferences
) : ThemeRepository {

    companion object {
        // Имя файла настроек для темы
        const val PREFS_NAME = "app_prefs"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    override fun isDarkThemeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_DARK_THEME, enabled)
            .apply()
    }

    override fun isThemeSet(): Boolean {
        return prefs.contains(KEY_DARK_THEME)
    }
}
