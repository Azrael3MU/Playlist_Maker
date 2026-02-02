package com.example.playlist_maker_main.media.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlist_maker_main.media.data.db.dao.PlaylistDao
import com.example.playlist_maker_main.media.data.db.dao.PlaylistTrackDao
import com.example.playlist_maker_main.media.data.db.dao.TrackDao
import com.example.playlist_maker_main.media.data.db.entity.PlaylistEntity
import com.example.playlist_maker_main.media.data.db.entity.PlaylistTrackEntity
import com.example.playlist_maker_main.media.data.db.entity.TrackEntity

@Database(version = 3, entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun PlaylistTrackDao(): PlaylistTrackDao
}