package com.example.playlist_maker_main.settings.domain

interface ThemeInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun setDarkThemeEnabled(enabled: Boolean)
    fun isThemeSet(): Boolean
}
