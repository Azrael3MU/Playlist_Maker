package com.example.playlist_maker_main.player.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.media.domain.db.FavoritesInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData(PlayerScreenState())
    val state: LiveData<PlayerScreenState> = _state

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private var mediaPlayer: MediaPlayer? = null
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
            _state.value = PlayerScreenState(
                isPlayButtonEnabled = false,
                isPlaying = false,
                currentPositionText = "00:00",
                errorMessage = "Нет ссылки на превью трека"
            )
            return
        }

        releasePlayer()

        _state.value = PlayerScreenState(
            isPlayButtonEnabled = false,
            isPlaying = false,
            currentPositionText = "00:00",
            errorMessage = null
        )

        val mp = MediaPlayer()
        mediaPlayer = mp

        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mp.setOnPreparedListener {
            _state.postValue(
                _state.value?.copy(isPlayButtonEnabled = true)
            )
        }

        mp.setOnCompletionListener {
            timerJob?.cancel()
            _state.postValue(
                _state.value?.copy(isPlaying = false, currentPositionText = "00:00")
            )
        }

        try {
            mp.setDataSource(track.previewUrl)
            mp.prepareAsync()
        } catch (e: Exception) {
            _state.postValue(
                PlayerScreenState(isPlayButtonEnabled = false, errorMessage = "Ошибка подготовки плеера")
            )
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
                        _state.value = _state.value?.copy(
                            currentPositionText = timeFormat.format(mp.currentPosition)
                        )
                    }
                }
                delay(UPDATE_PERIOD_MS)
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