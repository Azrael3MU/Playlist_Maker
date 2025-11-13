package com.example.playlist_maker_main.data.repository

import com.example.playlist_maker_main.data.network.ITunesApi
import com.example.playlist_maker_main.data.dto.TrackDto
import com.example.playlist_maker_main.data.mapper.toDomain
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.TracksRepository

class TracksRepositoryImpl(
    private val api: ITunesApi
) : TracksRepository {

    override suspend fun search(query: String): List<Track> {
        val resp = api.search(query)
        return resp.results!!.map(TrackDto::toDomain)
    }
}
