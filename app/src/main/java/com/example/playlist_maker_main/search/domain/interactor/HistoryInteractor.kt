package com.example.playlist_maker_main.search.domain.interactor

import com.example.playlist_maker_main.search.domain.model.Track

interface HistoryInteractor {
    suspend fun get(): List<Track>
    fun push(history: List<Track>, track: Track): List<Track>
    fun save(history: List<Track>)
    fun clear()
}