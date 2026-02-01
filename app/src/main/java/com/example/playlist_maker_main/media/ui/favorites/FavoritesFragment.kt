package com.example.playlist_maker_main.media.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentFavoritesBinding
import com.example.playlist_maker_main.player.ui.PlayerFragment
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.ui.TrackAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private var adapter: TrackAdapter? = null
    private var isClickAllowed = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        setupRecyclerView()

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    override fun onResume() {
        super.onResume()
        isClickAllowed = true
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                val args = bundleOf(PlayerFragment.ARG_TRACK to track)
                findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, args)
            }
        }
        binding.favoritesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecycler.adapter = adapter
    }

    private fun render(state: FavoritesState) {
        when (state) {
            is FavoritesState.Empty -> {
                binding.emptyFavoritesContainer.visibility = View.VISIBLE
                binding.favoritesRecycler.visibility = View.GONE
            }
            is FavoritesState.Content -> {
                binding.emptyFavoritesContainer.visibility = View.GONE
                binding.favoritesRecycler.visibility = View.VISIBLE

                adapter = TrackAdapter(state.tracks) { track ->
                    if (clickDebounce()) {
                        val args = bundleOf(PlayerFragment.ARG_TRACK to track)
                        findNavController().navigate(R.id.action_mediaFragment_to_playerFragment, args)
                    }
                }
                binding.favoritesRecycler.adapter = adapter
            }
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000L)
                isClickAllowed = true
            }
        }
        return current
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}