package com.example.playlist_maker_main.player.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.search.domain.model.Track

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TRACK = "extra_track"

        fun newIntent(context: Context, track: Track): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TRACK, track)
            }
        }
    }

    private lateinit var viewModel: PlayerViewModel

    private lateinit var ivBack: ImageView
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var ivPlay: ImageView
    private lateinit var tvProgress: TextView

    private lateinit var valueDuration: TextView
    private lateinit var labelAlbum: TextView
    private lateinit var valueAlbum: TextView
    private lateinit var labelYear: TextView
    private lateinit var valueYear: TextView
    private lateinit var labelGenre: TextView
    private lateinit var valueGenre: TextView
    private lateinit var labelCountry: TextView
    private lateinit var valueCountry: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]

        initViews()
        initInsets()
        initObservers()

        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK)
        if (track == null) {
            finish()
            return
        }

        bindTrackInfo(track)
        viewModel.init(track.previewUrl)   // если поле называется иначе — поменяй
    }

    private fun initViews() {
        val root: View = findViewById(R.id.player_root)

        ivBack = findViewById(R.id.arrow_back)
        ivCover = findViewById(R.id.artwork)
        tvTitle = findViewById(R.id.track_name)
        tvArtist = findViewById(R.id.artist_name)
        ivPlay = findViewById(R.id.play_btn)
        tvProgress = findViewById(R.id.track_time)

        valueDuration = findViewById(R.id.value_duration)
        labelAlbum = findViewById(R.id.label_album)
        valueAlbum = findViewById(R.id.value_album)
        labelYear = findViewById(R.id.label_year)
        valueYear = findViewById(R.id.value_year)
        labelGenre = findViewById(R.id.label_genre)
        valueGenre = findViewById(R.id.value_genre)
        labelCountry = findViewById(R.id.label_country)
        valueCountry = findViewById(R.id.value_country)

        ivBack.setOnClickListener { finish() }

        ivPlay.setOnClickListener {
            Log.d("PlayerActivity", "Play button clicked")
            viewModel.onPlayClicked()
        }
    }

    private fun initInsets() {
        val root: View = findViewById(R.id.player_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
    }

    private fun initObservers() {
        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: PlayerScreenState) {
        ivPlay.isEnabled = state.isPlayButtonEnabled
        setPlayIcon(state.isPlaying)
        tvProgress.text = state.currentPositionText

        if (!state.errorMessage.isNullOrBlank()) {
            Toast.makeText(this, state.errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.onErrorShown()
        }
    }

    private fun bindTrackInfo(t: Track) {
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
    }

    private fun setFieldOrHide(label: View, valueView: TextView, value: String?) {
        val has = !value.isNullOrBlank()
        label.visibility = if (has) View.VISIBLE else View.GONE
        valueView.visibility = if (has) View.VISIBLE else View.GONE
        if (has) valueView.text = value
    }

    private fun setPlayIcon(isPlaying: Boolean) {
        ivPlay.setImageResource(
            if (isPlaying) R.drawable.pause_btn else R.drawable.play_btn
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopView()
    }
}
