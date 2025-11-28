package com.example.playlist_maker_main.search.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.player.ui.PlayerFragment
import com.example.playlist_maker_main.search.domain.model.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var editText: EditText
    private lateinit var clearBtn: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressContainer: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var emptyContainer: View
    private lateinit var errorContainer: View
    private lateinit var historyContainer: View
    private lateinit var retryBtn: Button
    private lateinit var historyClearBtn: Button

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initViews(view)
        initListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        editText = view.findViewById(R.id.edit_text_id)
        clearBtn = view.findViewById(R.id.clear_btn)
        progressBar = view.findViewById(R.id.search_progress)
        progressContainer = view.findViewById(R.id.progress_container)

        recyclerView = view.findViewById(R.id.tracks_recycler_view)
        emptyContainer = view.findViewById(R.id.empty_container)
        errorContainer = view.findViewById(R.id.error_container)
        historyContainer = view.findViewById(R.id.history_container)
        retryBtn = view.findViewById(R.id.btn_retry)
        historyClearBtn = view.findViewById(R.id.btn_clear_history)

        adapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        historyRecycler = view.findViewById(R.id.history_recycler)
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = historyAdapter
    }

    private fun initListeners() {
        clearBtn.setOnClickListener {
            editText.setText("")
            hideKeyboard()
            viewModel.onQueryChanged("")
        }

        historyClearBtn.setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        retryBtn.setOnClickListener {
            viewModel.onRetry()
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                viewModel.onSearchSubmitted()
                true
            } else {
                false
            }
        }

        editText.addTextChangedListener(object : SimpleTextWatcher() {
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

    private fun renderState(state: SearchScreenState) {
        when (state) {
            is SearchScreenState.Idle -> {
                progressContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Loading -> {
                progressContainer.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Content -> {
                progressContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE

                adapter = TrackAdapter(state.tracks) { track -> onTrackClicked(track) }
                recyclerView.adapter = adapter
            }

            is SearchScreenState.EmptyResult -> {
                progressContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                emptyContainer.visibility = View.VISIBLE
                errorContainer.visibility = View.GONE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.Error -> {
                progressContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
                historyContainer.visibility = View.GONE
            }

            is SearchScreenState.History -> {
                progressContainer.visibility = View.GONE
                recyclerView.visibility = View.GONE
                emptyContainer.visibility = View.GONE
                errorContainer.visibility = View.GONE

                if (state.items.isEmpty()) {
                    historyContainer.visibility = View.GONE
                } else {
                    historyContainer.visibility = View.VISIBLE
                    historyAdapter = TrackAdapter(state.items) { track -> onTrackClicked(track) }
                    historyRecycler.adapter = historyAdapter
                }
            }
        }
    }

    private fun onTrackClicked(track: Track) {
        viewModel.onTrackClicked(track)
        val args = bundleOf(PlayerFragment.ARG_TRACK to track)
        findNavController().navigate(R.id.action_searchFragment_to_playerFragment, args)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
