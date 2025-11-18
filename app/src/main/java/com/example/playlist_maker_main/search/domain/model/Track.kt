package com.example.playlist_maker_main.search.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Long = 0L,
    val trackName: String = "",
    val artistName: String = "",
    val trackTimeMillis: Long = 0L,
    val artworkUrl100: String = "",
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
) : Parcelable {
    fun durationStr(): String {
        val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
        return sdf.format(trackTimeMillis)
    }

    fun cover512(): String =
        artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    fun year(): String? = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)
}
