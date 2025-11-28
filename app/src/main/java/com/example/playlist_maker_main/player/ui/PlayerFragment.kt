package com.example.playlist_maker_main.player.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.search.domain.model.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment(R.layout.fragment_player) {

    companion object {
        const val ARG_TRACK = "track"
    }

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var ivPlay: ImageView
    private lateinit var tvProgress: TextView

    private lateinit var valueDuration: TextView
    private lateinit var labelAlbum: View
    private lateinit var valueAlbum: TextView
    private lateinit var labelYear: View
    private lateinit var valueYear: TextView
    private lateinit var labelGenre: View
    private lateinit var valueGenre: TextView
    private lateinit var labelCountry: View
    private lateinit var valueCountry: TextView

    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = requireArguments().getParcelable(ARG_TRACK)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObservers()

        bindTrackInfo(track)
        viewModel.init(track.previewUrl)
    }

    private fun initViews(view: View) {
        // Никакого ivBack здесь нет
        ivCover = view.findViewById(R.id.artwork)
        tvTitle = view.findViewById(R.id.track_name)
        tvArtist = view.findViewById(R.id.artist_name)
        ivPlay = view.findViewById(R.id.play_btn)
        tvProgress = view.findViewById(R.id.track_time)

        valueDuration = view.findViewById(R.id.value_duration)
        labelAlbum = view.findViewById(R.id.label_album)
        valueAlbum = view.findViewById(R.id.value_album)
        labelYear = view.findViewById(R.id.label_year)
        valueYear = view.findViewById(R.id.value_year)
        labelGenre = view.findViewById(R.id.label_genre)
        valueGenre = view.findViewById(R.id.value_genre)
        labelCountry = view.findViewById(R.id.label_country)
        valueCountry = view.findViewById(R.id.value_country)

        ivPlay.setOnClickListener {
            viewModel.onPlayClicked()
        }
    }

    private fun initObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: PlayerScreenState) {
        ivPlay.isEnabled = state.isPlayButtonEnabled
        setPlayIcon(state.isPlaying)
        tvProgress.text = state.currentPositionText

        if (!state.errorMessage.isNullOrBlank()) {
            Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
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
