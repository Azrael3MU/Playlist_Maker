package com.example.playlist_maker_main.media.ui.favorites

import com.example.playlist_maker_main.search.domain.model.Track

sealed interface FavoritesState {
    data object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}