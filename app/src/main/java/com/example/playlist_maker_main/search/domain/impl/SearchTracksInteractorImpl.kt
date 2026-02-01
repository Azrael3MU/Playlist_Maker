package com.example.playlist_maker_main.search.domain.impl

import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchTracksInteractorImpl(
    private val repository: TracksRepository
) : SearchTracksInteractor {

    override fun searchTracks(expression: String): Flow<Pair<List<Track>?, String?>> {
        return repository.searchTracks(expression).map { result ->
            result.fold(
                onSuccess = { data ->
                    Pair(data, null)
                },
                onFailure = { error ->
                    Pair(null, error.message)
                }
            )
        }
    }
}