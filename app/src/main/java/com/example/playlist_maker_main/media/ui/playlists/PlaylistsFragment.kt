package com.example.playlist_maker_main.media.ui.playlists

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private val viewModel: PlaylistsViewModel by viewModel()
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistsBinding.bind(view)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.btnNewPlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_playlistFragment_to_newPlaylistFragment)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                showEmpty()
            } else {
                showContent(list)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fillData()
    }

    private fun showEmpty() {
        binding.emptyPlaylistsContainer.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showContent(list: List<com.example.playlist_maker_main.media.domain.model.Playlist>) {
        binding.emptyPlaylistsContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.recyclerView.adapter = PlaylistAdapter(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}