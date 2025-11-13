package com.example.playlist_maker_main.data.repository

import com.example.playlist_maker_main.data.mapper.toDomain
import com.example.playlist_maker_main.data.network.RetrofitProvider
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.TracksRepository

class TracksRepositoryImpl : TracksRepository {
    override suspend fun search(query: String): List<Track> {
        val dto = RetrofitProvider.api.search(query)
        return dto.results.orEmpty().map { it.toDomain() }
    }
}
