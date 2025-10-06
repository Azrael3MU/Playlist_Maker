package com.example.playlist_maker_main

import java.util.Locale
import java.text.SimpleDateFormat

private val fmt = SimpleDateFormat("mm:ss", Locale.getDefault())

fun TrackDto.toDomain(): Track {
    val ms = trackTimeMillis ?: 0L
    val mmss = fmt.format(ms)
    return Track(
        trackId = trackId ?: 0L,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTimeMillis   = trackTimeMillis ?: 0L,
        artworkUrl100 = artworkUrl100.orEmpty(),
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country
    )
}
