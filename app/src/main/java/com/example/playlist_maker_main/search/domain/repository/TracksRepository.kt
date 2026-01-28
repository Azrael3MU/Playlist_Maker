package com.example.playlist_maker_main.search.domain.repository

import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun searchTracks(expression: String): Flow<Result<List<Track>>>
}
