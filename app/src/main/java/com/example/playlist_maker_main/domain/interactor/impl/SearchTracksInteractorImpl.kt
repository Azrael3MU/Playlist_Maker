package com.example.playlist_maker_main.domain.interactor.impl

import com.example.playlist_maker_main.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.TracksRepository

class SearchTracksInteractorImpl(
    private val repo: TracksRepository
) : SearchTracksInteractor {

    override suspend fun execute(query: String): List<Track> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return repo.search(q)
    }

    override suspend fun search(query: String): List<Track> {
        return repo.search(query)
    }
}
