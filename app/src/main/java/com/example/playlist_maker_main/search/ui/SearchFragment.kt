package com.example.playlist_maker_main.search.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.player.ui.PlayerFragment
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()
    private var isClickAllowed = true

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
                SearchScreen(
                    viewModel = viewModel,
                    onTrackClick = { track -> onTrackClicked(track) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isClickAllowed = true
    }

    private fun onTrackClicked(track: Track) {
        if (clickDebounce()) {
            viewModel.onTrackClicked(track)
            val args = bundleOf(PlayerFragment.ARG_TRACK to track)
            findNavController().navigate(R.id.action_searchFragment_to_playerFragment, args)
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100L)
                isClickAllowed = true
            }
        }
        return current
    }
}