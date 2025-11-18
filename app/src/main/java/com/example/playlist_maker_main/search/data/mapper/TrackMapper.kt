package com.example.playlist_maker_main.search.data.mapper

import com.example.playlist_maker_main.search.data.dto.TrackDto
import com.example.playlist_maker_main.search.domain.model.Track

fun TrackDto.toDomain(): Track = Track(
    trackId = trackId ?: 0L,
    trackName = trackName.orEmpty(),
    artistName = artistName.orEmpty(),
    trackTimeMillis = trackTimeMillis ?: 0L,
    artworkUrl100 = artworkUrl100.orEmpty(),
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl
)
