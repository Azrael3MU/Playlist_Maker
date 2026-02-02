package com.example.playlist_maker_main.media.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.model.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(private val interactor: PlaylistInteractor) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    fun fillData() {
        viewModelScope.launch {
            interactor.getPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }
}