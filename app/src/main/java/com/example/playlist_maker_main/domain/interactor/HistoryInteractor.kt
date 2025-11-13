package com.example.playlist_maker_main.domain.interactor

import com.example.playlist_maker_main.domain.model.Track

interface HistoryInteractor {
    fun get(): List<Track>
    fun push(current: List<Track>, track: Track): List<Track>
    fun save(list: List<Track>)
    fun clear()
}
