package com.example.playlist_maker_main

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "extra_track"
        private const val TAG = "PlayerActivity"
        fun newIntent(ctx: android.content.Context, track: Track) =
            android.content.Intent(ctx, PlayerActivity::class.java).putExtra(EXTRA_TRACK, track)
    }

    private lateinit var ivBack: ImageView
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var ivPlay: ImageView
    private lateinit var tvProgress: TextView

    private lateinit var labelDuration: TextView
    private lateinit var valueDuration: TextView
    private lateinit var labelAlbum: TextView
    private lateinit var valueAlbum: TextView
    private lateinit var labelYear: TextView
    private lateinit var valueYear: TextView
    private lateinit var labelGenre: TextView
    private lateinit var valueGenre: TextView
    private lateinit var labelCountry: TextView
    private lateinit var valueCountry: TextView

    // ---- player ----
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false
    private var playing = false
    private var wantToPlay = false
    private var previewUrl: String? = null

    private val uiHandler = Handler(Looper.getMainLooper())
    private val timeFmt = SimpleDateFormat("mm:ss", Locale.getDefault())
    private val updateTask = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: return
            if (playing) {
                tvProgress.text = timeFmt.format(mp.currentPosition)
                uiHandler.postDelayed(this, 300L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        val root: View = findViewById(R.id.player_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        ivBack   = findViewById(R.id.arrow_back)
        ivCover  = findViewById(R.id.artwork)
        tvTitle  = findViewById(R.id.track_name)
        tvArtist = findViewById(R.id.artist_name)
        ivPlay   = findViewById(R.id.play_btn)
        tvProgress = findViewById(R.id.track_time)

        labelDuration = findViewById(R.id.label_duration)
        valueDuration = findViewById(R.id.value_duration)
        labelAlbum = findViewById(R.id.label_album)
        valueAlbum = findViewById(R.id.value_album)
        labelYear = findViewById(R.id.label_year)
        valueYear = findViewById(R.id.value_year)
        labelGenre = findViewById(R.id.label_genre)
        valueGenre = findViewById(R.id.value_genre)
        labelCountry = findViewById(R.id.label_country)
        valueCountry = findViewById(R.id.value_country)

        ivBack.setOnClickListener { finishWithStop() }

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK) ?: run {
            finish(); return
        }
        bind(track)

        // важное: делаем кнопку явно кликабельной
        ivPlay.isClickable = true
        ivPlay.isFocusable = true

        ivPlay.setOnClickListener {
            Log.d(TAG, "Play clicked. prepared=$prepared playing=$playing url=$previewUrl")
            if (previewUrl.isNullOrBlank()) {
                Toast.makeText(this, "Нет превью для трека", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!prepared) {
                // Подготовка ещё идёт — запомним намерение
                wantToPlay = true
                Toast.makeText(this, "Готовим плеер…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!playing) startPlayback() else pausePlayback()
        }
    }

    private fun bind(t: Track) {
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)
        Glide.with(this)
            .load(t.cover512())
            .placeholder(R.drawable.player_placeholder)
            .error(R.drawable.player_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(ivCover)

        tvTitle.text = t.trackName
        tvArtist.text = t.artistName

        valueDuration.text = t.durationStr()
        tvProgress.text = "00:00"

        setFieldOrHide(labelAlbum, valueAlbum, t.collectionName)
        setFieldOrHide(labelYear, valueYear, t.year())
        setFieldOrHide(labelGenre, valueGenre, t.primaryGenreName)
        setFieldOrHide(labelCountry, valueCountry, t.country)

        previewUrl = t.previewUrl
        Log.d(TAG, "bind previewUrl=$previewUrl")

        if (!previewUrl.isNullOrBlank()) {
            preparePlayer(previewUrl!!)
            ivPlay.isEnabled = true
        } else {
            ivPlay.isEnabled = false
        }
        setPlayIcon(false)
    }

    private fun setFieldOrHide(label: View, valueView: TextView, value: String?) {
        val has = !value.isNullOrBlank()
        label.visibility = if (has) View.VISIBLE else View.GONE
        valueView.visibility = if (has) View.VISIBLE else View.GONE
        if (has) valueView.text = value
    }

    private fun preparePlayer(url: String) {
        releasePlayer()
        prepared = false
        playing = false
        wantToPlay = false
        tvProgress.text = "00:00"
        setPlayIcon(false)

        val mp = MediaPlayer().also { mediaPlayer = it }

        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mp.setOnPreparedListener {
            prepared = true
            Log.d(TAG, "onPrepared()")
            if (wantToPlay) {
                wantToPlay = false
                startPlayback()
            }
        }

        mp.setOnCompletionListener {
            Log.d(TAG, "onCompletion()")
            playing = false
            setPlayIcon(false)
            tvProgress.text = "00:00"
            uiHandler.removeCallbacks(updateTask)
            mp.seekTo(0)
        }

        mp.setOnErrorListener { _, what, extra ->
            Log.e(TAG, "onError what=$what extra=$extra")
            Toast.makeText(this, "Ошибка плеера ($what/$extra)", Toast.LENGTH_SHORT).show()
            playing = false
            prepared = false
            wantToPlay = false
            setPlayIcon(false)
            uiHandler.removeCallbacks(updateTask)
            true
        }

        try {
            Log.d(TAG, "setDataSource($url)")
            mp.setDataSource(url)
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "prepareAsync failed: ${e.message}", e)
            Toast.makeText(this, "Не удалось подготовить плеер", Toast.LENGTH_SHORT).show()
            ivPlay.isEnabled = false
        }
    }

    private fun startPlayback() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.currentPosition >= mp.duration && mp.duration > 0) mp.seekTo(0)
            mp.start()
            playing = true
            setPlayIcon(true)
            uiHandler.post(updateTask)
            Log.d(TAG, "Playback started")
        } catch (e: Exception) {
            Log.e(TAG, "start failed: ${e.message}", e)
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            try { it.pause() } catch (_: Exception) {}
        }
        playing = false
        setPlayIcon(false)
        uiHandler.removeCallbacks(updateTask)
        Log.d(TAG, "Playback paused")
    }

    private fun finishWithStop() {
        pausePlayback()
        finish()
    }

    private fun releasePlayer() {
        uiHandler.removeCallbacks(updateTask)
        mediaPlayer?.release()
        mediaPlayer = null
        prepared = false
        playing = false
    }

    private fun setPlayIcon(isPlaying: Boolean) {
        ivPlay.setImageResource(if (isPlaying) R.drawable.pause_btn else R.drawable.play_btn)
    }

    override fun onStop() {
        super.onStop()
        if (playing) pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}
