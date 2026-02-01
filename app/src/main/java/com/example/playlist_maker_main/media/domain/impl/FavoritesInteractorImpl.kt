package com.example.playlist_maker_main.media.domain.impl

import com.example.playlist_maker_main.media.domain.db.FavoritesInteractor
import com.example.playlist_maker_main.media.domain.db.FavoritesRepository
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val favoritesRepository: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun addTrack(track: Track) {
        favoritesRepository.addTrack(track)
    }

    override suspend fun deleteTrack(track: Track) {
        favoritesRepository.deleteTrack(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoritesRepository.getFavoriteTracks()
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return favoritesRepository.isFavorite(trackId)
    }
}