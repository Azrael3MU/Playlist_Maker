package com.example.playlist_maker_main.domain.interactor.impl

import com.example.playlist_maker_main.domain.interactor.HistoryInteractor
import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.HistoryRepository

class HistoryInteractorImpl(
    private val repo: HistoryRepository,
    private val capacity: Int = 10
) : HistoryInteractor {

    override fun get(): List<Track> = repo.load()

    override fun push(current: List<Track>, track: Track): List<Track> {
        val res = current.toMutableList()
        res.removeAll { it.trackId == track.trackId }
        res.add(0, track)
        val trimmed = if (res.size > capacity) res.take(capacity) else res
        return trimmed
    }

    override fun save(list: List<Track>) = repo.save(list)

    override fun clear() = repo.save(emptyList())
}
