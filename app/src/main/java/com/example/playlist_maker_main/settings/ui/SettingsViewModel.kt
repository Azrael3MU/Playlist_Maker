package com.example.playlist_maker_main.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlist_maker_main.settings.domain.ThemeInteractor

class SettingsViewModel(
    private val themeInteractor: ThemeInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SettingsScreenState>()
    val state: LiveData<SettingsScreenState> = _state

    init {
        val isDark = themeInteractor.isDarkThemeEnabled()
        _state.value = SettingsScreenState(isDark)
    }

    fun onThemeToggled(isDark: Boolean) {
        themeInteractor.setDarkThemeEnabled(isDark)
        _state.value = SettingsScreenState(isDark)
    }
}
