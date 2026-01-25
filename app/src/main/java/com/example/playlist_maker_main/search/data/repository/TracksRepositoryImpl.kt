package com.example.playlist_maker_main.search.data.repository

import com.example.playlist_maker_main.search.data.dto.TrackDto
import com.example.playlist_maker_main.search.data.mapper.toDomain // Или используй маппер внутри
import com.example.playlist_maker_main.search.data.network.ITunesApi
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository
import com.example.playlist_maker_main.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val api: ITunesApi
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<Resource<List<Track>>> = flow {
        try {
            val response = api.search(expression)
            if (response.results != null) {
                val data = response.results.map { it.toDomain() }
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error("Ошибка сервера"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Ошибка подключения: ${e.message}"))
        }
    }
}