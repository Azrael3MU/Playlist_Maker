package com.example.playlist_maker_main.search.domain.impl

import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository

class SearchTracksInteractorImpl(
    private val repo: TracksRepository
) : SearchTracksInteractor {

    override suspend fun execute(query: String): List<Track> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return repo.search(q)
    }
}
