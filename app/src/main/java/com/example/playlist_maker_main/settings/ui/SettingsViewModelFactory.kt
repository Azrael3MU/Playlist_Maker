package com.example.playlist_maker_main.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlist_maker_main.settings.domain.ThemeInteractor

class SettingsViewModelFactory(
    private val themeInteractor: ThemeInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(themeInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
