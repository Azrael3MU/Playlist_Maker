package com.example.playlist_maker_main.search.domain.interactor

import com.example.playlist_maker_main.search.domain.model.Track

interface SearchTracksInteractor {

    suspend fun execute(query: String): List<Track>
}

