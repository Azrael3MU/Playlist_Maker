package com.example.playlist_maker_main.search.data.repository

import android.content.SharedPreferences
import com.example.playlist_maker_main.media.data.db.AppDatabase
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val appDatabase: AppDatabase // <--- Добавили БД
) : HistoryRepository {

    companion object {
        const val SEARCH_HISTORY_KEY = "search_history_key"
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        val historyTracks: List<Track> = gson.fromJson(json, type)
        return historyTracks
    }

    override fun saveHistory(tracks: List<Track>) {
        val json = gson.toJson(tracks)
        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }

    override fun clearHistory() {
        sharedPreferences.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }
}