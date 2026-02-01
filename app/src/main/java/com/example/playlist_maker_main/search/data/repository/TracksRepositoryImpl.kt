package com.example.playlist_maker_main.search.data.repository

import com.example.playlist_maker_main.media.data.db.AppDatabase
import com.example.playlist_maker_main.search.data.network.ITunesApi
import com.example.playlist_maker_main.search.data.mapper.toDomain
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val api: ITunesApi,
    private val appDatabase: AppDatabase
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<Result<List<Track>>> = flow {
        try {
            val response = api.search(expression)
            if (response.results != null) {
                val tracks = response.results.map { it.toDomain() }

                val favoriteIds = appDatabase.trackDao().getTracksIds()

                tracks.forEach { track ->
                    track.isFavorite = favoriteIds.contains(track.trackId)
                }

                emit(Result.success(tracks))
            } else {
                emit(Result.failure(Exception("Ошибка сервера")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Ошибка подключения: ${e.message}")))
        }
    }
}