package com.example.playlist_maker_main

import java.text.SimpleDateFormat
import java.util.Locale

private val timeFmt = SimpleDateFormat("mm:ss", Locale.getDefault())

fun TrackDto.toDomain(): Track = Track(
    trackId = trackId ?: 0L,
    trackName = trackName.orEmpty(),
    artistName = artistName.orEmpty(),
    trackTime = timeFmt.format((trackTimeMillis ?: 0L)),
    artworkUrl100 = artworkUrl100.orEmpty()
)
