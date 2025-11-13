package com.example.playlist_maker_main.domain.repository

import com.example.playlist_maker_main.domain.model.Track

interface HistoryRepository {
    fun load(): List<Track>
    fun save(list: List<Track>)
}
