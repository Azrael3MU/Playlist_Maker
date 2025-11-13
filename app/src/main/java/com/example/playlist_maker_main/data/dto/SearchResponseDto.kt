package com.example.playlist_maker_main.data.dto

data class SearchResponseDto(
    val resultCount: Int?,
    val results: List<TrackDto>?
)
