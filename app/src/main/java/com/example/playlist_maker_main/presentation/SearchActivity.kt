package com.example.playlist_maker_main.presentation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.di.Creator
import com.example.playlist_maker_main.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.presentation.adapter.TrackAdapter
import com.example.playlist_maker_main.presentation.util.SimpleTextWatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_MS = 2000L
        private const val CLICK_DEBOUNCE_MS = 700L
    }

    private val searchInteractor by lazy { Creator.searchTracksInteractor }
    private lateinit var historyInteractor: HistoryInteractor

    private lateinit var editText: EditText
    private lateinit var clearBtn: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressContainer: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var emptyContainer: View
    private lateinit var errorContainer: View
    private lateinit var retryBtn: View

    private lateinit var historyContainer: View
    private lateinit var historyRecycler: RecyclerView
    private lateinit var historyAdapter: TrackAdapter

    private var history: MutableList<Track> = mutableListOf()
    private var currentSearchText = ""
    private var lastQuery = ""
    private var searchJob: Job? = null

    private val clickGuard = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // DI
        historyInteractor = Creator.provideHistoryInteractor(this)
        history = historyInteractor.get().toMutableList()

        // Insets
        val root: View = findViewById(R.id.searchRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top)
            insets
        }

        // Views
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
        val historyClearBtn = findViewById<Button>(R.id.btn_clear_history)

        // Lists
        adapter = TrackAdapter(emptyList()) { onTrackClicked(it) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        historyAdapter = TrackAdapter(history) { onTrackClicked(it) }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        // Actions
        backBtn.setOnClickListener { finish() }

        historyClearBtn.setOnClickListener {
            historyInteractor.clear()
            history.clear()
            historyAdapter.submitList(history.toList())
            showHistoryIfNeeded()
        }

        clearBtn.setOnClickListener {
            searchJob?.cancel()
            editText.text.clear()
            editText.clearFocus()
            hideKeyboard(editText)
            render()
            lastQuery = ""
            showHistoryIfNeeded()
        }

        editText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString().orEmpty()
                clearBtn.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE
                scheduleDebouncedSearch(currentSearchText)
                showHistoryIfNeeded()
            }
        })

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchJob?.cancel()
                performSearch(editText.text.toString())
                true
            } else false
        }

        retryBtn.setOnClickListener {
            if (lastQuery.isNotBlank()) performSearch(lastQuery)
        }

        historyAdapter.submitList(history.toList())
        showHistoryIfNeeded()
    }

    private fun scheduleDebouncedSearch(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            render()
            showHistoryIfNeeded()
            return
        }
        searchJob = lifecycleScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(query)
        }
    }

    private fun onTrackClicked(track: Track) {
        if (clickGuard.getAndSet(true)) return
        handler.postDelayed({ clickGuard.set(false) }, CLICK_DEBOUNCE_MS)

        // История через интерактор
        history = historyInteractor.push(history, track).toMutableList()
        historyInteractor.save(history)
        historyAdapter.submitList(history.toList())
        showHistoryIfNeeded()

        startActivity(PlayerActivity.newIntent(this, track))
    }

    private fun performSearch(queryRaw: String) {
        val query = queryRaw.trim()
        lastQuery = query
        if (query.isBlank()) {
            render()
            showHistoryIfNeeded()
            return
        }

        progressContainer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        render()

        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            try {
                val tracks = searchInteractor.search(query)
                if (tracks.isEmpty()) render(showEmpty = true) else render(list = tracks)
            } catch (_: Exception) {
                render(showError = true)
            } finally {
                progressBar.visibility = View.GONE
                progressContainer.visibility = View.GONE
            }
        }
    }

    private fun render(
        list: List<Track> = emptyList(),
        showEmpty: Boolean = false,
        showError: Boolean = false
    ) {
        adapter.submitList(list)
        recyclerView.visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        emptyContainer.visibility = if (showEmpty) View.VISIBLE else View.GONE
        errorContainer.visibility = if (showError) View.VISIBLE else View.GONE
        if (list.isNotEmpty() || showEmpty || showError) historyContainer.visibility = View.GONE
    }

    private fun showHistoryIfNeeded() {
        val nothingShown = recyclerView.visibility != View.VISIBLE &&
                emptyContainer.visibility != View.VISIBLE &&
                errorContainer.visibility != View.VISIBLE

        val shouldShow = currentSearchText.isEmpty() && history.isNotEmpty() && nothingShown
        historyContainer.visibility = if (shouldShow) View.VISIBLE else View.GONE
        if (shouldShow) {
            recyclerView.visibility = View.GONE
            emptyContainer.visibility = View.GONE
            errorContainer.visibility = View.GONE
            historyContainer.bringToFront()
        }
    }

    private fun hideKeyboard(v: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(v.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restored = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        editText.setText(restored)
        currentSearchText = restored
        clearBtn.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE
        showHistoryIfNeeded()
    }
}
