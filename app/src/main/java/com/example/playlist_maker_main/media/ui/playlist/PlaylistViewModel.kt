package com.example.playlist_maker_main.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Int,
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _closeScreen = MutableLiveData<Unit>()
    val closeScreen: LiveData<Unit> = _closeScreen


    fun fillData() {
        viewModelScope.launch {
            val playlistData = interactor.getPlaylistById(playlistId)
            _playlist.postValue(playlistData)

            val trackList = interactor.getTracksByIds(playlistData.trackIds).first()
            _tracks.postValue(trackList)
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            interactor.deleteTrackFromPlaylist(trackId, playlistId)

            val currentPlaylist = _playlist.value
            if (currentPlaylist != null) {
                val updatedIds = currentPlaylist.trackIds.filter { it != trackId }
                _playlist.postValue(
                    currentPlaylist.copy(
                        trackIds = updatedIds,
                        tracksCount = updatedIds.size
                    )
                )
            }

            val currentTracks = _tracks.value
            if (currentTracks != null) {
                val updatedTracks = currentTracks.filter { it.trackId != trackId }
                _tracks.postValue(updatedTracks)
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            interactor.deletePlaylist(playlistId)
            _closeScreen.postValue(Unit)
        }
    }
}