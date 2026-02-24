package com.example.playlist_maker_main.media.domain.model

data class Playlist(
    val id: Int = 0,
    val name: String,
    val description: String?,
    val imagePath: String?,
    val trackIds: List<Long>,
    val tracksCount: Int
)