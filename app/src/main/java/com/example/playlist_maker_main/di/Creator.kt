package com.example.playlist_maker_main.di

import android.content.Context
import com.example.playlist_maker_main.data.network.RetrofitProvider
import com.example.playlist_maker_main.data.repository.HistoryRepositoryImpl
import com.example.playlist_maker_main.data.repository.TracksRepositoryImpl
import com.example.playlist_maker_main.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.domain.interactor.impl.HistoryInteractorImpl
import com.example.playlist_maker_main.domain.interactor.impl.SearchTracksInteractorImpl
import com.example.playlist_maker_main.domain.repository.HistoryRepository
import com.example.playlist_maker_main.domain.repository.TracksRepository
import com.google.gson.Gson

object Creator {

    private val tracksRepository: TracksRepository by lazy {
        TracksRepositoryImpl(RetrofitProvider.api)
    }

    val searchTracksInteractor: SearchTracksInteractor by lazy {
        SearchTracksInteractorImpl(tracksRepository)
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        val prefs = context.getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
        val repo: HistoryRepository = HistoryRepositoryImpl(prefs, Gson())
        return HistoryInteractorImpl(repo, capacity = 10)
    }
}
