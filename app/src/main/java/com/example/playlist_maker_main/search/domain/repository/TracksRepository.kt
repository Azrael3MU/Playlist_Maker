package com.example.playlist_maker_main.search.domain.repository

import com.example.playlist_maker_main.search.domain.model.Track

interface TracksRepository {
    suspend fun search(query: String): List<Track>
}
