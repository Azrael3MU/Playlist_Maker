package com.example.playlist_maker_main.search.domain.repository

import com.example.playlist_maker_main.search.domain.model.Track

interface HistoryRepository {
    suspend fun getHistory(): List<Track>
    fun saveHistory(tracks: List<Track>)
    fun clearHistory()
}