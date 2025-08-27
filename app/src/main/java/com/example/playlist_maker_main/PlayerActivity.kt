package com.example.playlist_maker_main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlayerActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var ivAdd: ImageView
    private lateinit var ivFav: ImageView
    private lateinit var ivPlay: ImageView
    private lateinit var tvTime: TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        ivBack = findViewById(R.id.arrow_back)
        ivCover = findViewById(R.id.artwork)
        tvTitle = findViewById(R.id.track_name)
        tvArtist = findViewById(R.id.artist_name)
        ivAdd = findViewById(R.id.add_playlist)
        ivFav = findViewById(R.id.favorite_btn)
        ivPlay = findViewById(R.id.play_btn)
        tvTime = findViewById(R.id.track_time)

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

        ivBack.setOnClickListener { finish() }

        val root: View = findViewById(R.id.player_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        val track = intent.getParcelableExtra<Track>("track") ?: run {
            finish()
            return
        }

        bind(track)
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
        tvTime.text = t.durationStr()

        setFieldOrHide(labelAlbum, valueAlbum, t.collectionName)
        setFieldOrHide(labelYear, valueYear, t.year())
        setFieldOrHide(labelGenre, valueGenre, t.primaryGenreName)
        setFieldOrHide(labelCountry, valueCountry, t.country)
        valueDuration.text = t.durationStr()
    }

    private fun setFieldOrHide(label: View, valueView: TextView, value: String?) {
        val has = !value.isNullOrBlank()
        label.visibility = if (has) View.VISIBLE else View.GONE
        valueView.visibility = if (has) View.VISIBLE else View.GONE
        if (has) valueView.text = value
    }
}
