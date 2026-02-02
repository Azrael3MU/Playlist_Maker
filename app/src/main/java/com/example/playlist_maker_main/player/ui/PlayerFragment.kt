package com.example.playlist_maker_main.player.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentPlayerBinding
import com.example.playlist_maker_main.search.domain.model.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment(R.layout.fragment_player) {

    companion object {
        const val ARG_TRACK = "track"
    }

    private val viewModel: PlayerViewModel by viewModel()

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = requireArguments().getParcelable(ARG_TRACK)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        initObservers()
        bindTrackInfo(track)

        binding.playBtn.setOnClickListener {
            viewModel.onPlayClicked()
        }

        binding.favoriteBtn.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        viewModel.init(track)
    }

    private fun initObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            renderFavorite(isFavorite)
        }
    }

    private fun render(state: PlayerScreenState) = with(binding) {
        playBtn.isEnabled = state.isPlayButtonEnabled
        setPlayIcon(state.isPlaying)
        trackTime.text = state.currentPositionText

        if (!state.errorMessage.isNullOrBlank()) {
            Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
            viewModel.onErrorShown()
        }
    }

    private fun renderFavorite(isFavorite: Boolean) {
        val icon = if (isFavorite) R.drawable.favorite_act_btn else R.drawable.favorite_btn
        binding.favoriteBtn.setImageResource(icon)
    }

    private fun bindTrackInfo(t: Track) = with(binding) {
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)

        Glide.with(this@PlayerFragment)
            .load(t.cover512())
            .placeholder(R.drawable.player_placeholder)
            .error(R.drawable.player_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(artwork)

        trackName.text = t.trackName
        artistName.text = t.artistName
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
        binding.playBtn.setImageResource(
            if (isPlaying) R.drawable.pause_btn else R.drawable.play_btn
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}