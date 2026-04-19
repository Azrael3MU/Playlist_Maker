package com.example.playlist_maker_main.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.media.ui.favorites.FavoritesViewModel
import com.example.playlist_maker_main.media.ui.playlists.PlaylistsViewModel
import com.example.playlist_maker_main.player.ui.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                MediaScreen(
                    favoritesViewModel = favoritesViewModel,
                    playlistsViewModel = playlistsViewModel,
                    onNavigateToNewPlaylist = {
                        findNavController().navigate(R.id.action_mediaFragment_to_newPlaylistFragment)
                    },
                    onNavigateToPlayer = { track ->
                        val args = bundleOf(PlayerFragment.ARG_TRACK to track)
                        findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, args)
                    },
                    onNavigateToPlaylistDetails = { playlistId ->
                        val bundle = bundleOf("playlistId" to playlistId)
                        findNavController().navigate(R.id.action_PlaylistFragment_to_PlaylistDetailsFragment, bundle)
                    }
                )
            }
        }
    }
}