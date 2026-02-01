package com.example.playlist_maker_main.search.domain.interactor

import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchTracksInteractor {
    fun searchTracks(expression: String): Flow<Pair<List<Track>?, String?>>
}