package com.example.playlist_maker_main.media.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.model.Playlist
import kotlinx.coroutines.launch

class NewPlaylistViewModel(private val interactor: PlaylistInteractor) : ViewModel() {

    fun createPlaylist(name: String, description: String?, imagePath: String?) {
        viewModelScope.launch {
            interactor.addPlaylist(
                Playlist(
                    name = name,
                    description = description,
                    imagePath = imagePath,
                    trackIds = emptyList(),
                    tracksCount = 0
                )
            )
        }
    }
}