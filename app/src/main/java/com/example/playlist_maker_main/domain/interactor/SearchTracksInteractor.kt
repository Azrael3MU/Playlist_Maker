package com.example.playlist_maker_main.domain.interactor

import com.example.playlist_maker_main.domain.model.Track

interface SearchTracksInteractor {
    suspend fun search(query: String): List<Track>

    suspend fun execute(query: String): List<Track>
}
