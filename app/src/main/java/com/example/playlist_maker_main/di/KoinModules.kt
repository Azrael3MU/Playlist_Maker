package com.example.playlist_maker_main.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlist_maker_main.search.data.network.RetrofitProvider
import com.example.playlist_maker_main.search.data.repository.HistoryRepositoryImpl
import com.example.playlist_maker_main.search.data.repository.TracksRepositoryImpl
import com.example.playlist_maker_main.search.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.search.domain.impl.HistoryInteractorImpl
import com.example.playlist_maker_main.search.domain.impl.SearchTracksInteractorImpl
import com.example.playlist_maker_main.search.domain.repository.HistoryRepository
import com.example.playlist_maker_main.search.domain.repository.TracksRepository
import com.example.playlist_maker_main.settings.data.ThemeRepositoryImpl
import com.example.playlist_maker_main.settings.domain.ThemeInteractor
import com.example.playlist_maker_main.settings.domain.impl.ThemeInteractorImpl
import com.example.playlist_maker_main.settings.domain.repository.ThemeRepository
import com.example.playlist_maker_main.player.ui.PlayerViewModel
import com.example.playlist_maker_main.search.ui.SearchViewModel
import com.example.playlist_maker_main.settings.ui.SettingsViewModel
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {

    single { Gson() }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(get(), get())
    }

    single<ThemeRepository> {
        val prefs = androidContext().getSharedPreferences(
            ThemeRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        ThemeRepositoryImpl(prefs)
    }

    single { RetrofitProvider.api }
    single<TracksRepository> { TracksRepositoryImpl(get()) }
}


val domainModule = module {

    single<SearchTracksInteractor> { SearchTracksInteractorImpl(get()) }

    single<HistoryInteractor> { HistoryInteractorImpl(get(), capacity = 10) }

    single<ThemeInteractor> { ThemeInteractorImpl(get()) }
}


val presentationModule = module {

    viewModel { SearchViewModel(get(), get()) }

    viewModel { SettingsViewModel(get()) }

    viewModel { PlayerViewModel() }
}
