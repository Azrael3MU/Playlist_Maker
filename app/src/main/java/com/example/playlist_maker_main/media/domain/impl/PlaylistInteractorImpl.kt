package com.example.playlist_maker_main.media.domain.impl

import com.example.playlist_maker_main.media.domain.db.PlaylistInteractor
import com.example.playlist_maker_main.media.domain.db.PlaylistRepository
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(private val repository: PlaylistRepository) : PlaylistInteractor {

    override suspend fun addPlaylist(playlist: Playlist) {
        repository.addPlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        repository.addTrackToPlaylist(track, playlist)
    }
}