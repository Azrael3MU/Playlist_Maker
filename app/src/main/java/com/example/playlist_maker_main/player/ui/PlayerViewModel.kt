package com.example.playlist_maker_main.player.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
        private const val UPDATE_PERIOD_MS = 300L
    }

    private val _state = MutableLiveData(PlayerScreenState())
    val state: LiveData<PlayerScreenState> = _state

    private var mediaPlayer: MediaPlayer? = null
    private var previewUrl: String? = null
    private var isPrepared = false
    private var isPlaying = false

    private val uiHandler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val updateTask = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: return
            if (!mp.isPlaying) return

            val pos = mp.currentPosition
            val text = timeFormat.format(pos)

            _state.value = _state.value?.copy(currentPositionText = text)
            uiHandler.postDelayed(this, UPDATE_PERIOD_MS)
        }
    }

    fun init(previewUrl: String?) {
        this.previewUrl = previewUrl
        Log.d(TAG, "init() previewUrl = $previewUrl")

        if (previewUrl.isNullOrBlank()) {
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
        isPrepared = false
        isPlaying = false

        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mp.setOnPreparedListener {
            Log.d(TAG, "onPrepared()")
            isPrepared = true
            _state.postValue(
                _state.value?.copy(
                    isPlayButtonEnabled = true,
                    errorMessage = null
                )
            )
        }

        mp.setOnCompletionListener {
            Log.d(TAG, "onCompletion()")
            isPlaying = false
            uiHandler.removeCallbacks(updateTask)
            _state.postValue(
                _state.value?.copy(
                    isPlaying = false,
                    currentPositionText = "00:00"
                )
            )
        }

        try {
            mp.setDataSource(previewUrl)
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "prepareAsync failed: ${e.message}", e)
            _state.postValue(
                PlayerScreenState(
                    isPlayButtonEnabled = false,
                    isPlaying = false,
                    currentPositionText = "00:00",
                    errorMessage = "Ошибка подготовки плеера"
                )
            )
        }
    }

    fun onPlayClicked() {
        val mp = mediaPlayer
        val url = previewUrl
        Log.d(TAG, "onPlayClicked() mp=$mp url=$url isPrepared=$isPrepared")

        if (mp == null || url.isNullOrBlank()) {
            _state.value = _state.value?.copy(
                errorMessage = "Плеер не готов"
            )
            return
        }
        if (!isPrepared) {
            _state.value = _state.value?.copy(
                errorMessage = "Трек ещё загружается..."
            )
            return
        }

        if (!isPlaying) {
            startPlayback(mp)
        } else {
            pausePlayback(mp)
        }
    }

    fun onStopView() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                pausePlayback(mp)
            }
        }
    }

    fun onErrorShown() {
        val cur = _state.value ?: return
        if (cur.errorMessage != null) {
            _state.value = cur.copy(errorMessage = null)
        }
    }

    private fun startPlayback(mp: MediaPlayer) {
        try {
            mp.start()
            isPlaying = true
            _state.value = _state.value?.copy(
                isPlaying = true,
                errorMessage = null
            )
            uiHandler.post(updateTask)
            Log.d(TAG, "startPlayback() started")
        } catch (e: Exception) {
            Log.e(TAG, "startPlayback() error: ${e.message}", e)
            isPlaying = false
            _state.value = _state.value?.copy(
                isPlaying = false,
                errorMessage = "Не удалось начать воспроизведение"
            )
        }
    }

    private fun pausePlayback(mp: MediaPlayer) {
        try {
            mp.pause()
            isPlaying = false
            uiHandler.removeCallbacks(updateTask)
            _state.value = _state.value?.copy(isPlaying = false)
            Log.d(TAG, "pausePlayback() paused")
        } catch (e: Exception) {
            Log.e(TAG, "pausePlayback() error: ${e.message}", e)
            _state.value = _state.value?.copy(
                errorMessage = "Ошибка паузы плеера"
            )
        }
    }

    private fun releasePlayer() {
        uiHandler.removeCallbacks(updateTask)
        try {
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
        isPrepared = false
        isPlaying = false
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
