package com.example.playlist_maker_main.search.data.repository

import com.example.playlist_maker_main.search.data.network.ITunesApi
import com.example.playlist_maker_main.search.data.mapper.toDomain
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val api: ITunesApi
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<Result<List<Track>>> = flow {
        try {
            val response = api.search(expression)
            if (response.results != null) {
                val data = response.results.map { it.toDomain() }
                emit(Result.success(data))
            } else {
                emit(Result.failure(Exception("Ошибка сервера")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Ошибка подключения: ${e.message}")))
        }
    }
}