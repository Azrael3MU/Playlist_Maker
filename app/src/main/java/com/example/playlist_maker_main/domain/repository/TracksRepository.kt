package com.example.playlist_maker_main.domain.repository

import com.example.playlist_maker_main.domain.model.Track

interface TracksRepository {
    suspend fun search(query: String): List<Track>
}
