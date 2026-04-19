package com.example.playlist_maker_main.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.FavoritesInteractor
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData(PlayerScreenState())
    val state: LiveData<PlayerScreenState> = _state

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addingResult = MutableLiveData<Pair<String, Boolean>>()
    val addingResult: LiveData<Pair<String, Boolean>> = _addingResult

    private var currentTrack: Track? = null

    private var audioPlayerControl: AudioPlayerControl? = null

    fun init(track: Track) {
        this.currentTrack = track
        _isFavorite.value = track.isFavorite

        viewModelScope.launch {
            val actualFavoriteStatus = favoritesInteractor.isFavorite(track.trackId)
            _isFavorite.postValue(actualFavoriteStatus)
            track.isFavorite = actualFavoriteStatus
        }

        if (track.previewUrl.isNullOrBlank()) {
            _state.value = PlayerScreenState(isPlayButtonEnabled = false, currentPositionText = "00:00")
        }
    }

    fun setAudioPlayerControl(control: AudioPlayerControl) {
        audioPlayerControl = control

        viewModelScope.launch {
            control.getPlayerState().collect { serviceState ->
                val currentState = _state.value ?: PlayerScreenState()
                _state.value = currentState.copy(
                    isPlayButtonEnabled = serviceState.isPrepared,
                    isPlaying = serviceState.isPlaying,
                    currentPositionText = serviceState.currentPositionText
                )
            }
        }
    }

    fun removeAudioPlayerControl() {
        audioPlayerControl = null
    }

    fun getPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        val track = currentTrack ?: return

        if (playlist.trackIds.contains(track.trackId)) {
            _addingResult.postValue("Трек уже добавлен в плейлист ${playlist.name}" to false)
        } else {
            viewModelScope.launch {
                playlistInteractor.addTrackToPlaylist(track, playlist)
                _addingResult.postValue("Добавлено в плейлист ${playlist.name}" to true)
                getPlaylists()
            }
        }
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoritesInteractor.deleteTrack(track)
                track.isFavorite = false
                _isFavorite.value = false
            } else {
                favoritesInteractor.addTrack(track)
                track.isFavorite = true
                _isFavorite.value = true
            }
        }
    }

    fun onPlayClicked() {
        if (_state.value?.isPlaying == true) {
            audioPlayerControl?.pausePlayer()
        } else {
            audioPlayerControl?.startPlayer()
        }
    }

    fun onAppBackgrounded() {
        audioPlayerControl?.showNotification()
    }

    fun onAppForegrounded() {
        audioPlayerControl?.hideNotification()
    }

    fun onErrorShown() {
        _state.value = _state.value?.copy(errorMessage = null)
    }
}