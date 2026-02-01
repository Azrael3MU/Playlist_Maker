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

    single {
        androidx.room.Room.databaseBuilder(androidContext(), com.example.playlist_maker_main.media.data.db.AppDatabase::class.java, "database.db")
            .build()
    }

    factory { com.example.playlist_maker_main.media.data.converters.TrackDbConverter() }

    single<com.example.playlist_maker_main.media.domain.db.FavoritesRepository> {
        com.example.playlist_maker_main.media.data.repository.FavoritesRepositoryImpl(get(), get())
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(get(), get(), get())
    }

    single<ThemeRepository> {
        val prefs = androidContext().getSharedPreferences(
            ThemeRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        ThemeRepositoryImpl(prefs)
    }

    single { RetrofitProvider.api }

    single<TracksRepository> { TracksRepositoryImpl(get(), get()) }
}


val domainModule = module {

    single<SearchTracksInteractor> { SearchTracksInteractorImpl(get()) }

    single<HistoryInteractor> { HistoryInteractorImpl(get(), capacity = 10) }

    single<ThemeInteractor> { ThemeInteractorImpl(get()) }

    single<com.example.playlist_maker_main.media.domain.db.FavoritesInteractor> {
        com.example.playlist_maker_main.media.domain.impl.FavoritesInteractorImpl(get())
    }
}

val presentationModule = module {

    viewModel { SearchViewModel(get(), get()) }

    viewModel { SettingsViewModel(get()) }

    viewModel { PlayerViewModel(get()) }

    viewModel { com.example.playlist_maker_main.media.ui.MediaViewModel() }
    viewModel { com.example.playlist_maker_main.media.ui.favorites.FavoritesViewModel(get()) }
    viewModel { com.example.playlist_maker_main.media.ui.playlists.PlaylistsViewModel() }
}
