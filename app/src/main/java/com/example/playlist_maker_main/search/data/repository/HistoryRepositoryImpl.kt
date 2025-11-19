package com.example.playlist_maker_main.search.data.repository

import android.content.SharedPreferences
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson,
) : HistoryRepository {

    private val KEY = "search_history"

    override fun load(): List<Track> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun save(list: List<Track>) {
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }
}
