package com.example.playlist_maker_main.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlist_maker_main.search.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 2000L
    }

    private val _state = MutableLiveData<SearchScreenState>(SearchScreenState.Idle)
    val state: LiveData<SearchScreenState> = _state

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var searchJob: Job? = null
    private var lastQuery: String = ""

    init {
        showHistory()
    }

    fun onQueryChanged(text: String) {
        lastQuery = text

        if (text.isBlank()) {
            searchJob?.cancel()
            showHistory()
        } else {
            startSearchWithDebounce(text)
        }
    }

    fun onSearchSubmitted() {
        val q = lastQuery.trim()
        if (q.isEmpty()) {
            showHistory()
        } else {
            startSearchWithDebounce(q)
        }
    }

    fun onRetry() {
        val q = lastQuery.trim()
        if (q.isNotEmpty()) {
            startSearchWithDebounce(q)
        }
    }

    fun onClearHistoryClicked() {
        historyInteractor.clear()
        _state.value = SearchScreenState.History(emptyList())
    }

    fun onTrackClicked(track: Track) {
        val currentHistory = historyInteractor.get()
        val updated = historyInteractor.push(currentHistory, track)
        historyInteractor.save(updated)
    }

    private fun showHistory() {
        val list = historyInteractor.get()
        _state.value = if (list.isEmpty()) {
            SearchScreenState.Idle
        } else {
            SearchScreenState.History(list)
        }
    }

    private fun startSearchWithDebounce(query: String) {
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(SEARCH_DEBOUNCE_MS)

            val q = query.trim()
            if (q.isEmpty()) {
                showHistory()
                return@launch
            }

            _state.value = SearchScreenState.Loading

            try {
                val tracks = withContext(Dispatchers.IO) {
                    searchInteractor.execute(q)
                }

                _state.value = if (tracks.isEmpty()) {
                    SearchScreenState.EmptyResult
                } else {
                    SearchScreenState.Content(tracks)
                }
            } catch (e: Exception) {
                _state.value = SearchScreenState.Error
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
