package com.example.playlist_maker_main.search.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.FragmentSearchBinding
import com.example.playlist_maker_main.player.ui.PlayerFragment
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var isClickAllowed = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        initViews()
        initListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        isClickAllowed = true
    }

    private fun initViews() = with(binding) {
        adapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = adapter

        historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = historyAdapter
    }

    private fun initListeners() = with(binding) {
        clearBtn.setOnClickListener {
            editTextId.setText("")
            hideKeyboard()
            viewModel.onQueryChanged("")
        }

        btnClearHistory.setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        btnRetry.setOnClickListener {
            viewModel.onRetry()
        }

        editTextId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                viewModel.onSearchSubmitted()
                true
            } else {
                false
            }
        }

        editTextId.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString().orEmpty()
                clearBtn.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                viewModel.onQueryChanged(text)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: SearchScreenState) = with(binding) {
        when (state) {
            is SearchScreenState.Idle -> {
                progressContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Loading -> {
                progressContainer.visibility = View.VISIBLE
                searchProgress.visibility = View.VISIBLE
                tracksRecyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Content -> {
                progressContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.VISIBLE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE

                adapter = TrackAdapter(state.tracks) { track -> onTrackClicked(track) }
                tracksRecyclerView.adapter = adapter
            }

            is SearchScreenState.EmptyResult -> {
                progressContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                emptyContainer.visibility = View.VISIBLE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Error -> {
                progressContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.History -> {
                progressContainer.visibility = View.GONE
                tracksRecyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE

                if (state.items.isEmpty()) {
                    historyContainer.visibility = View.GONE
                } else {
                    historyContainer.visibility = View.VISIBLE
                    historyAdapter =
                        TrackAdapter(state.items) { track -> onTrackClicked(track) }
                    historyRecycler.adapter = historyAdapter
                }
            }
        }
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
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTextId.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 100L
    }

}