package com.example.playlist_maker_main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "playlist_maker_prefs"
        private const val HISTORY_KEY = "search_history"
        private const val HISTORY_MAX = 10
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

    private lateinit var editText: EditText
    private lateinit var clearBtn: ImageView
    private lateinit var backBtn: ImageView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var emptyContainer: View
    private lateinit var errorContainer: View
    private lateinit var retryBtn: View

    private lateinit var historyContainer: View
    private lateinit var historyRecycler: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private var history: MutableList<Track> = mutableListOf()

    private var currentSearchText = ""
    private var lastQuery = ""
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val root: View = findViewById(R.id.searchRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(top = bars.top)
            insets
        }

        backBtn = findViewById(R.id.back_button)
        editText = findViewById(R.id.edit_text_id)
        clearBtn = findViewById(R.id.clear_btn)

        recyclerView = findViewById(R.id.tracks_recycler_view)
        emptyContainer = findViewById(R.id.empty_container)
        errorContainer = findViewById(R.id.error_container)
        retryBtn = findViewById(R.id.btn_retry)

        historyContainer = findViewById(R.id.history_container)
        historyRecycler = findViewById(R.id.history_recycler)
        val historyClearBtn = findViewById<Button>(R.id.btn_clear_history)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        history = loadHistory()

        adapter = TrackAdapter(emptyList()) { track ->
            pushToHistory(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        historyAdapter = TrackAdapter(history) { track ->
            pushToHistory(track)
        }
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        backBtn.setOnClickListener { finish() }

        clearBtn.setOnClickListener {
            searchJob?.cancel()
            editText.text.clear()
            editText.clearFocus()
            hideKeyboard(editText)

            render()
            lastQuery = ""
            showHistoryIfNeeded()
        }

        historyClearBtn.setOnClickListener {
            clearHistory()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString().orEmpty()
                clearBtn.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE
                showHistoryIfNeeded()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        editText.setOnFocusChangeListener { _, _ -> showHistoryIfNeeded() }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
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

    private fun loadHistory(): MutableList<Track> {
        val json = prefs.getString(HISTORY_KEY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveHistory() {
        prefs.edit().putString(HISTORY_KEY, gson.toJson(history)).apply()
    }

    private fun pushToHistory(track: Track) {
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > HISTORY_MAX) history = history.take(HISTORY_MAX).toMutableList()
        saveHistory()
        historyAdapter.submitList(history.toList())
        showHistoryIfNeeded()
    }

    private fun clearHistory() {
        history.clear()
        saveHistory()
        historyAdapter.submitList(history.toList())
        showHistoryIfNeeded()
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
        if (list.isNotEmpty() || showEmpty || showError) {
            historyContainer.visibility = View.GONE
        }
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


    private fun performSearch(queryRaw: String) {
        val query = queryRaw.trim()
        lastQuery = query
        if (query.isBlank()) {
            render()
            showHistoryIfNeeded()
            return
        }

        historyContainer.visibility = View.GONE
        render()
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            try {
                val response = Network.api.search(query)
                val tracks = response.results.map { it.toDomain() }
                if (tracks.isEmpty()) render(showEmpty = true) else render(list = tracks)
            } catch (_: Exception) {
                render(showError = true)
            }
        }
    }

    private fun hideKeyboard(v: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
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
