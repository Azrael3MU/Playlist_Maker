package com.example.playlist_maker_main.search.ui

import com.example.playlist_maker_main.search.domain.model.Track

sealed class SearchScreenState {
    data object Idle : SearchScreenState()
    data object Loading : SearchScreenState()
    data class Content(
        val tracks: List<Track>
    ) : SearchScreenState()
    data object EmptyResult : SearchScreenState()
    data object Error : SearchScreenState()
    data class History(
        val items: List<Track>
    ) : SearchScreenState()
}
