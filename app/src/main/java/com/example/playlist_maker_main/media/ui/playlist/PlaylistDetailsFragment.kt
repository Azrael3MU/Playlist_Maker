package com.example.playlist_maker_main.media.ui.playlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentPlaylistDetailsBinding
import com.example.playlist_maker_main.player.ui.PlayerFragment
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.ui.TrackAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailsFragment : Fragment(R.layout.fragment_playlist_details) {

    private val playlistId by lazy { requireArguments().getInt("playlistId") }
    private val viewModel: PlaylistViewModel by viewModel { parametersOf(playlistId) }
    private var binding: FragmentPlaylistDetailsBinding? = null

    private var trackAdapter: TrackAdapter? = null
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlaylistDetailsBinding.bind(view)

        setupMenuBottomSheet()
        setupTracksRecyclerView()

        viewModel.fillData()

        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            binding?.tvPlaylistNameLarge?.text = playlist.name
            binding?.tvPlaylistDescription?.text = playlist.description

            Glide.with(this)
                .load(playlist.imagePath)
                .placeholder(R.drawable.placeholder)
                .into(binding!!.ivPlaylistCoverLarge)

            binding?.tvTitleMenu?.text = playlist.name
            val countText = resources.getQuantityString(R.plurals.track_count, playlist.tracksCount, playlist.tracksCount)
            binding?.tvCountMenu?.text = countText
            Glide.with(this)
                .load(playlist.imagePath)
                .placeholder(R.drawable.placeholder)
                .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
                .into(binding!!.ivCoverMenu)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { trackList ->
            val totalMillis = trackList.sumOf { it.trackTimeMillis }
            val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(totalMillis).toInt()
            val durationText = resources.getQuantityString(R.plurals.minutes_count, minutes, minutes)
            val countText = resources.getQuantityString(R.plurals.track_count, trackList.size, trackList.size)
            binding?.tvPlaylistInfo?.text = "$durationText • $countText"

            if (trackList.isEmpty()) {
                binding?.tvEmptyPlaylistMessage?.visibility = View.VISIBLE
                binding?.rvTracks?.visibility = View.GONE
            } else {
                binding?.tvEmptyPlaylistMessage?.visibility = View.GONE
                binding?.rvTracks?.visibility = View.VISIBLE
                trackAdapter?.submitList(trackList)
            }
        }

        binding?.btnBack?.setOnClickListener { findNavController().popBackStack() }

        binding?.btnShare?.setOnClickListener { sharePlaylist() }
        binding?.btnShareMenu?.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        binding?.btnMenu?.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding?.btnDeleteMenu?.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        binding?.btnEditMenu?.setOnClickListener {
            val playlist = viewModel.playlist.value
            if (playlist != null) {
                val bundle = androidx.core.os.bundleOf("playlist" to playlist)
                findNavController().navigate(R.id.action_playlistDetailsFragment_to_newPlaylistFragment, bundle)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { findNavController().popBackStack() }
        })
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding!!.menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding?.overlay?.visibility = if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding?.overlay?.alpha = slideOffset + 1f
            }
        })
    }

    private fun setupTracksRecyclerView() {
        trackAdapter = TrackAdapter(
            tracks = emptyList(),
            onClick = { track ->
                val bundle = bundleOf(PlayerFragment.ARG_TRACK to track)
                findNavController().navigate(R.id.action_playlistDetailsFragment_to_playerFragment, bundle)
            },
            onLongClick = { track ->
                showDeleteTrackDialog(track)
            }
        )
        binding?.rvTracks?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvTracks?.adapter = trackAdapter
    }

    private fun sharePlaylist() {
        val playlist = viewModel.playlist.value
        val tracks = viewModel.tracks.value
        if (playlist == null || tracks.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "В этом плейлисте нет списка треков, которым можно поделиться", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append(playlist.name).append("\n")
        if (!playlist.description.isNullOrBlank()) sb.append(playlist.description).append("\n")
        sb.append(resources.getQuantityString(R.plurals.track_count, tracks.size, tracks.size)).append("\n\n")

        tracks.forEachIndexed { index, track ->
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} (${track.durationStr()})\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Поделиться"))
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_track_title))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ -> viewModel.deleteTrack(track.trackId) }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_title))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deletePlaylist()
                findNavController().popBackStack()
            }
            .show()
    }
}