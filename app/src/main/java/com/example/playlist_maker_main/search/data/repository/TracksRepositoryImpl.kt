package com.example.playlist_maker_main.search.data.repository

import com.example.playlist_maker_main.search.data.network.ITunesApi
import com.example.playlist_maker_main.search.data.dto.TrackDto
import com.example.playlist_maker_main.search.data.mapper.toDomain
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository

class TracksRepositoryImpl(
    private val api: ITunesApi
) : TracksRepository {

    override suspend fun search(query: String): List<Track> {
        val resp = api.search(query)
        return resp.results!!.map(TrackDto::toDomain)
    }
}
