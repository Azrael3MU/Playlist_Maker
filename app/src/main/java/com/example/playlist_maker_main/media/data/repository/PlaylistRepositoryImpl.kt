package com.example.playlist_maker_main.media.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.example.playlist_maker_main.media.data.db.AppDatabase
import com.example.playlist_maker_main.media.data.db.dao.PlaylistDao
import com.example.playlist_maker_main.media.data.db.dao.PlaylistTrackDao
import com.example.playlist_maker_main.media.data.db.entity.PlaylistEntity
import com.example.playlist_maker_main.media.data.db.entity.PlaylistTrackEntity
import com.example.playlist_maker_main.media.domain.db.PlaylistRepository
import com.example.playlist_maker_main.media.domain.model.Playlist
import com.example.playlist_maker_main.search.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream

class PlaylistRepositoryImpl(
    private val context: Context,
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun addPlaylist(playlist: Playlist) {
        val entity = convertToEntity(playlist)
        playlistDao.insertPlaylist(entity)
    }

    override fun saveImageToPrivateStorage(uri: Uri): String {
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "playlist_covers")
        if (!filePath.exists()) filePath.mkdirs()
        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                BitmapFactory.decodeStream(inputStream)
                    .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
            }
        }
        return file.absolutePath
    }

    override fun getPlaylists(): Flow<List<Playlist>> = flow {
        val entities = playlistDao.getPlaylists()
        emit(entities.map { convertToDomain(it) })
    }

    private fun convertToEntity(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            imagePath = playlist.imagePath,
            trackIds = gson.toJson(playlist.trackIds),
            tracksCount = playlist.tracksCount
        )
    }

    private fun convertToDomain(entity: PlaylistEntity): Playlist {
        val type = object : TypeToken<List<Long>>() {}.type
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            imagePath = entity.imagePath,
            trackIds = gson.fromJson(entity.trackIds, type),
            tracksCount = entity.tracksCount
        )
    }
    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistTrackDao.insertTrack(convertToPlaylistTrackEntity(track))

        val updatedIds = playlist.trackIds.toMutableList()
        updatedIds.add(track.trackId)

        val updatedPlaylist = playlist.copy(
            trackIds = updatedIds,
            tracksCount = updatedIds.size
        )

        playlistDao.updatePlaylist(convertToEntity(updatedPlaylist))
    }
    private fun convertToPlaylistTrackEntity(track: Track): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }
}