package com.example.playlist_maker_main.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.playlist_maker_main.media.data.db.entity.PlaylistTrackEntity

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)
}