package com.example.playlist_maker_main.domain.interactor

import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.TracksRepository

class SearchTracksInteractor(
    private val repo: TracksRepository
) {
    suspend fun search(query: String): List<Track> = repo.search(query)
}
