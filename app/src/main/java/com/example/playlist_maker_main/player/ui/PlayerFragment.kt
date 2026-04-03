package com.example.playlist_maker_main.player.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentPlayerBinding
import com.example.playlist_maker_main.media.ui.playlists.PlaylistAdapterBS
import com.example.playlist_maker_main.search.domain.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment(R.layout.fragment_player) {

    companion object {
        const val ARG_TRACK = "track"
    }

    private val viewModel: PlayerViewModel by viewModel()
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var track: Track
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = requireArguments().getParcelable(ARG_TRACK)!!

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as AudioPlayerService.LocalBinder
                viewModel.setAudioPlayerControl(binder.getService())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                viewModel.removeAudioPlayerControl()
            }
        }

        val intent = Intent(requireContext(), AudioPlayerService::class.java).apply {
            putExtra("URL", track.previewUrl)
            putExtra("TRACK_NAME", track.trackName)
            putExtra("ARTIST_NAME", track.artistName)
        }
        requireContext().bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setupBottomSheet()
        initObservers()
        bindTrackInfo(track)

        binding.playBtn.setOnClickListener { viewModel.onPlayClicked() }
        binding.favoriteBtn.setOnClickListener { viewModel.onFavoriteClicked() }

        binding.addPlaylist.setOnClickListener {
            viewModel.getPlaylists()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.btnNewPlaylistBs.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
        }

        viewModel.init(track)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let {
            requireContext().unbindService(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                _binding?.overlay?.visibility = if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.overlay?.alpha = (slideOffset + 1f) * 0.6f
            }
        })
    }

    private fun initObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state -> render(state) }
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite -> renderFavorite(isFavorite) }

        viewModel.playlists.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) return@observe

            val adapter = PlaylistAdapterBS(list) { playlist ->
                viewModel.addTrackToPlaylist(playlist)
            }

            binding.rvPlaylistsBs.adapter = adapter
            binding.rvPlaylistsBs.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }

        viewModel.addingResult.observe(viewLifecycleOwner) { (message, success) ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (success) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun render(state: PlayerScreenState) = with(binding) {
        playBtn.isEnabled = state.isPlayButtonEnabled
        playBtn.setState(state.isPlaying) // Метод из прошлой задачи с CustomView
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
            .load(t.cover512()).placeholder(R.drawable.player_placeholder)
            .transform(CenterCrop(), RoundedCorners(radius)).into(artwork)

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
}