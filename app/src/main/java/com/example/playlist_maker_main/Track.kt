package com.example.playlist_maker_main

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,

    val trackTime: String,
    val trackTimeMillis: Long = 0L,

    val artworkUrl100: String,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null
) : Parcelable {

    fun cover512(): String = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    fun durationStr(): String = trackTime.ifBlank {
        val total = (trackTimeMillis.coerceAtLeast(0L) / 1000)
        "%02d:%02d".format(total / 60, total % 60)
    }

    fun year(): String? = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
}
