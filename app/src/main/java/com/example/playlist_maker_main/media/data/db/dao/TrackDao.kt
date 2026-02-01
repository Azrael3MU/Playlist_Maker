package com.example.playlist_maker_main.media.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlist_maker_main.media.data.db.entity.TrackEntity

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks_table ORDER BY timeAdded DESC")
    fun getTracks(): kotlinx.coroutines.flow.Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks_table")
    suspend fun getTracksIds(): List<Long>
}