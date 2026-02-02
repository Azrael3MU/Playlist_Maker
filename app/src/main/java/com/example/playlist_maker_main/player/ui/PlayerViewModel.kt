package com.example.playlist_maker_main.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.FavoritesInteractor
import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor // Импорт нового интерактора
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var currentTrack: Track? = null
    private var timerJob: Job? = null
    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

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
            return
        }

        releasePlayer()
        val mp = android.media.MediaPlayer()
        mediaPlayer = mp
        mp.setAudioAttributes(android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build())

        mp.setOnPreparedListener { _state.postValue(_state.value?.copy(isPlayButtonEnabled = true)) }
        mp.setOnCompletionListener {
            timerJob?.cancel()
            _state.postValue(_state.value?.copy(isPlaying = false, currentPositionText = "00:00"))
        }

        try {
            mp.setDataSource(track.previewUrl)
            mp.prepareAsync()
        } catch (e: Exception) {
            _state.postValue(PlayerScreenState(isPlayButtonEnabled = false))
        }
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
                getPlaylists() // Обновляем список, чтобы счетчик треков изменился
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
        val mp = mediaPlayer ?: return
        if (_state.value?.isPlaying == true) {
            mp.pause()
            timerJob?.cancel()
            _state.value = _state.value?.copy(isPlaying = false)
        } else {
            mp.start()
            _state.value = _state.value?.copy(isPlaying = true)
            startTimer()
        }
    }

    fun onStopView() {
        if (_state.value?.isPlaying == true) {
            mediaPlayer?.pause()
            timerJob?.cancel()
            _state.value = _state.value?.copy(isPlaying = false)
        }
    }

    fun onErrorShown() {
        _state.value = _state.value?.copy(errorMessage = null)
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_state.value?.isPlaying == true) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _state.value = _state.value?.copy(currentPositionText = timeFormat.format(mp.currentPosition))
                    }
                }
                delay(300L)
            }
        }
    }

    private fun releasePlayer() {
        timerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    companion object {
        private const val UPDATE_PERIOD_MS = 300L
    }
}