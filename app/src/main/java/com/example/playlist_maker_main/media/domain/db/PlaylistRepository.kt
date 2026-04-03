package com.example.playlist_maker_main.media.domain.db

import android.net.Uri
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun addPlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    fun saveImageToPrivateStorage(uri: Uri): String
    suspend fun getPlaylistById(id: Int): Playlist
    fun getTracksByIds(ids: List<Long>): Flow<List<Track>>
    suspend fun deleteTrackFromPlaylist(trackId: Long, playlistId: Int)
    suspend fun deletePlaylist(playlistId: Int)
}