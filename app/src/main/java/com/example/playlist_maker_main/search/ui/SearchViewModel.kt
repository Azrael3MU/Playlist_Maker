package com.example.playlist_maker_main.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker_main.search.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 2000L
    }

    private val _state = MutableLiveData<SearchScreenState>(SearchScreenState.Idle)
    val state: LiveData<SearchScreenState> = _state

    private var searchJob: Job? = null
    private var lastQuery: String = ""

    init {
        showHistory()
    }

    fun onQueryChanged(text: String) {
        if (lastQuery == text) return
        lastQuery = text

        if (text.isBlank()) {
            searchJob?.cancel()
            showHistory()
        } else {
            searchDebounce(text)
        }
    }

    fun onSearchSubmitted() {
        val q = lastQuery.trim()
        if (q.isEmpty()) {
            showHistory()
        } else {
            searchJob?.cancel()
            searchRequest(q)
        }
    }

    fun onRetry() {
        val q = lastQuery.trim()
        if (q.isNotEmpty()) {
            searchRequest(q)
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

    private fun searchDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchRequest(query)
        }
    }

    private fun searchRequest(query: String) {
        if (query.isEmpty()) return

        _state.value = SearchScreenState.Loading

        viewModelScope.launch {
            searchInteractor.searchTracks(query)
                .collect { pair ->
                    processResult(pair.first, pair.second)
                }
        }
    }

    private fun processResult(foundTracks: List<Track>?, errorMessage: String?) {
        val tracks = foundTracks
        if (tracks != null) {
            if (tracks.isEmpty()) {
                _state.value = SearchScreenState.EmptyResult
            } else {
                _state.value = SearchScreenState.Content(tracks)
            }
        } else {
            _state.value = SearchScreenState.Error
        }
    }
}