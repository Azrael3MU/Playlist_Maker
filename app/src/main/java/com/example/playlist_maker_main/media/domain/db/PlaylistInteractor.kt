package com.example.playlist_maker_main.media.domain.db

import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun addPlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
}