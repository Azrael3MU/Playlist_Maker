package com.example.playlist_maker_main.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Int,
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    fun fillData() {
        viewModelScope.launch {
            val playlistData = interactor.getPlaylistById(playlistId)
            _playlist.postValue(playlistData)

            interactor.getTracksByIds(playlistData.trackIds).collect { trackList ->
                _tracks.postValue(trackList)
            }
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            interactor.deleteTrackFromPlaylist(trackId, playlistId)
            fillData()
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            interactor.deletePlaylist(playlistId)
        }
    }
}