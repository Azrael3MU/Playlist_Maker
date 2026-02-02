package com.example.playlist_maker_main.media.data.repository

import com.example.playlist_maker_main.media.data.converters.TrackDbConverter
import com.example.playlist_maker_main.media.data.db.AppDatabase
import com.example.playlist_maker_main.media.data.db.dao.TrackDao
import com.example.playlist_maker_main.media.domain.db.FavoritesRepository
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val trackDao: TrackDao,
    private val trackDbConverter: TrackDbConverter
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        val entity = trackDbConverter.map(track, System.currentTimeMillis())
        trackDao.insertTrack(entity)
    }

    override suspend fun deleteTrack(track: Track) {
        val entity = trackDbConverter.map(track, System.currentTimeMillis())
        trackDao.deleteTrack(entity)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getTracks().map { entities ->
            entities.map { trackDbConverter.map(it) }
        }
    }
    override suspend fun isFavorite(trackId: Long): Boolean {
        return trackDao.getTracksIds().contains(trackId)
    }
}