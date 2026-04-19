package com.example.playlist_maker_main.player.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlist_maker_main.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale

data class ServicePlayerState(
    val isPrepared: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPositionText: String = "00:00"
)

interface AudioPlayerControl {
    fun getPlayerState(): StateFlow<ServicePlayerState>
    fun startPlayer()
    fun pausePlayer()
    fun showNotification()
    fun hideNotification()
}

class AudioPlayerService : Service(), AudioPlayerControl {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow(ServicePlayerState())

    private var trackName = ""
    private var artistName = ""
    private var trackUrl = ""

    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerControl = this@AudioPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        trackUrl = intent?.getStringExtra("URL") ?: ""
        trackName = intent?.getStringExtra("TRACK_NAME") ?: ""
        artistName = intent?.getStringExtra("ARTIST_NAME") ?: ""

        initMediaPlayer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        releasePlayer()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        releasePlayer()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun initMediaPlayer() {
        if (trackUrl.isBlank()) return

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnPreparedListener {
                _playerState.value = _playerState.value.copy(isPrepared = true)
            }
            setOnCompletionListener {
                timerJob?.cancel()
                _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionText = "00:00")
                hideNotification()
            }
            try {
                setDataSource(trackUrl)
                prepareAsync()
            } catch (e: Exception) {
            }
        }
    }

    override fun getPlayerState(): StateFlow<ServicePlayerState> = _playerState.asStateFlow()

    override fun startPlayer() {
        mediaPlayer?.start()
        _playerState.value = _playerState.value.copy(isPlaying = true)
        startTimer()
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
        timerJob?.cancel()
    }

    override fun showNotification() {
        if (_playerState.value.isPlaying) {
            createNotificationChannel()
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Playlist Maker")
                .setContentText("$artistName - $trackName")
                .setSmallIcon(R.mipmap.icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                else 0
            )
        }
    }

    override fun hideNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playerState.value = _playerState.value.copy(
                            currentPositionText = timeFormat.format(mp.currentPosition)
                        )
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playlist Maker Audio",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "audio_channel"
        private const val NOTIFICATION_ID = 1
    }
}