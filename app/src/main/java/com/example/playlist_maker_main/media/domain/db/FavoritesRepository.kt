package com.example.playlist_maker_main.media.domain.db

import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Long): Boolean
}