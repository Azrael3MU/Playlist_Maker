package com.example.playlist_maker_main.data.repository

import android.content.SharedPreferences
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryRepositoryImpl(
    private val prefs: SharedPreferences,
    private val gson: Gson = Gson(),
    private val key: String = "search_history"
) : HistoryRepository {

    override fun load(): List<Track> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    override fun save(list: List<Track>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }
}
