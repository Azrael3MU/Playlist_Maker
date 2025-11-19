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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.player.ui.PlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var backBtn: ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val root: View = findViewById(R.id.searchRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        initViews()
        initListeners()
        observeViewModel()
    }

    private fun initViews() {
        backBtn = findViewById(R.id.back_button)
        editText = findViewById(R.id.edit_text_id)
        clearBtn = findViewById(R.id.clear_btn)
        progressBar = findViewById(R.id.search_progress)
        progressContainer = findViewById(R.id.progress_container)

        recyclerView = findViewById(R.id.tracks_recycler_view)
        emptyContainer = findViewById(R.id.empty_container)
        errorContainer = findViewById(R.id.error_container)
        retryBtn = findViewById(R.id.btn_retry)

        historyContainer = findViewById(R.id.history_container)
        historyRecycler = findViewById(R.id.history_recycler)
        historyClearBtn = findViewById(R.id.btn_clear_history)

        adapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        historyAdapter = TrackAdapter(emptyList()) { track -> onTrackClicked(track) }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter
    }

    private fun initListeners() {
        backBtn.setOnClickListener { finish() }

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
        viewModel.state.observe(this) { state ->
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
        startActivity(PlayerActivity.newIntent(this, track))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
