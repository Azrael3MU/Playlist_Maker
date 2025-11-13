package com.example.playlist_maker_main.di

import android.content.Context
import com.example.playlist_maker_main.data.repository.HistoryRepositoryImpl
import com.example.playlist_maker_main.data.repository.TracksRepositoryImpl
import com.example.playlist_maker_main.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.domain.repository.HistoryRepository
import com.example.playlist_maker_main.domain.repository.TracksRepository

object Creator {
    private const val PREFS_NAME = "playlist_maker_prefs"

    fun provideTracksRepository(): TracksRepository = TracksRepositoryImpl()

    fun provideHistoryRepository(ctx: Context): HistoryRepository =
        HistoryRepositoryImpl(ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun provideSearchInteractor(): SearchTracksInteractor =
        SearchTracksInteractor(provideTracksRepository())

    fun provideHistoryInteractor(ctx: Context): HistoryInteractor =
        HistoryInteractor(provideHistoryRepository(ctx))
}
