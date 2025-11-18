package com.example.playlist_maker_main.creator

import android.content.Context
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

    private var themeRepository: ThemeRepository? = null

    private fun getThemeRepository(context: Context): ThemeRepository {
        val cached = themeRepository
        if (cached != null) return cached

        val prefs = context.getSharedPreferences(
            ThemeRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val repo = ThemeRepositoryImpl(prefs)
        themeRepository = repo
        return repo
    }

    fun provideThemeInteractor(context: Context): ThemeInteractor {
        val repo = getThemeRepository(context)
        return ThemeInteractorImpl(repo)
    }
}
