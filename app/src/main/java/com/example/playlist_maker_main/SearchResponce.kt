package com.example.playlist_maker_main

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)
