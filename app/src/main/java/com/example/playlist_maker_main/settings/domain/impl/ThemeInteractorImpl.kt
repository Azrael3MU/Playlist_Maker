package com.example.playlist_maker_main.settings.domain.impl

import com.example.playlist_maker_main.settings.domain.ThemeInteractor
import com.example.playlist_maker_main.settings.domain.repository.ThemeRepository

class ThemeInteractorImpl(
    private val repo: ThemeRepository
) : ThemeInteractor {

    override fun isDarkThemeEnabled(): Boolean = repo.isDarkThemeEnabled()

    override fun setDarkThemeEnabled(enabled: Boolean) {
        repo.setDarkThemeEnabled(enabled)
    }

    override fun isThemeSet(): Boolean = repo.isThemeSet()
}
