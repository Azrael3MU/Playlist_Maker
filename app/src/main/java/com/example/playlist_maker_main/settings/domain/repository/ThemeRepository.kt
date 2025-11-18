package com.example.playlist_maker_main.settings.domain.repository

interface ThemeRepository {
    fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(enabled: Boolean)
    fun isThemeSet(): Boolean
}
