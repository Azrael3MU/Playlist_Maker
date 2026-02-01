package com.example.playlist_maker_main.media.data.repository

import com.example.playlist_maker_main.media.data.converters.TrackDbConverter
import com.example.playlist_maker_main.media.data.db.AppDatabase
import com.example.playlist_maker_main.media.domain.db.FavoritesRepository
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val trackDbConverter: TrackDbConverter
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        val entity = trackDbConverter.map(track)
        appDatabase.trackDao().insertTrack(entity)
    }

    override suspend fun deleteTrack(track: Track) {
        val entity = trackDbConverter.map(track)
        appDatabase.trackDao().deleteTrack(entity)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return appDatabase.trackDao().getTracks().map { entities ->
            entities.map { trackDbConverter.map(it) }
        }
    }
}