package com.example.playlist_maker_main.media.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.FavoritesInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>()
    val state: LiveData<FavoritesState> = _state

    init {
        viewModelScope.launch {
            favoritesInteractor.getFavoriteTracks()
                .collect { tracks ->
                    processResult(tracks)
                }
        }
    }

    private fun processResult(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            _state.value = FavoritesState.Empty
        } else {
            _state.value = FavoritesState.Content(tracks)
        }
    }
}