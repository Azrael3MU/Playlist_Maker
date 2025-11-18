package com.example.playlist_maker_main.search.domain.repository

import com.example.playlist_maker_main.search.domain.model.Track

interface HistoryRepository {
    fun load(): List<Track>
    fun save(list: List<Track>)
}
