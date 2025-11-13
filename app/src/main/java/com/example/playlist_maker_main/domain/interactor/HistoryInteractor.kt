package com.example.playlist_maker_main.domain.interactor

import com.example.playlist_maker_main.domain.model.Track
import com.example.playlist_maker_main.domain.repository.HistoryRepository

class HistoryInteractor(
    private val repo: HistoryRepository,
    private val capacity: Int = 10
) {
    fun get(): List<Track> = repo.load()

    fun push(current: List<Track>, track: Track): List<Track> {
        val res = current.toMutableList()
        res.removeAll { it.trackId == track.trackId }
        res.add(0, track)
        return if (res.size > capacity) res.take(capacity) else res
    }

    fun save(list: List<Track>) = repo.save(list)
    fun clear() = repo.save(emptyList())
}
