package com.example.playlist_maker_main.search.domain.impl

import com.example.playlist_maker_main.search.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.search.domain.model.Track
import com.example.playlist_maker_main.search.domain.repository.HistoryRepository

class HistoryInteractorImpl(
    private val repository: HistoryRepository,
    private val capacity: Int = 10
) : HistoryInteractor {

    override suspend fun get(): List<Track> {
        return repository.getHistory()
    }

    override fun push(history: List<Track>, track: Track): List<Track> {
        val mutableHistory = history.toMutableList()
        mutableHistory.removeIf { it.trackId == track.trackId }
        mutableHistory.add(0, track)
        if (mutableHistory.size > capacity) {
            mutableHistory.removeAt(mutableHistory.lastIndex)
        }
        return mutableHistory
    }

    override fun save(history: List<Track>) {
        repository.saveHistory(history)
    }

    override fun clear() {
        repository.clearHistory()
    }
}