package com.example.playlist_maker_main.search.domain.repository

import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.util.Resource
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun searchTracks(query: String): Flow<Resource<List<Track>>>
}
