package com.example.playlist_maker_main.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlist_maker_main.search.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.search.domain.interactor.SearchTracksInteractor

class SearchViewModelFactory(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchInteractor, historyInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
