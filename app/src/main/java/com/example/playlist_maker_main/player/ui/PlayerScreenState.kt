package com.example.playlist_maker_main.player.ui

data class PlayerScreenState(
    val isPlayButtonEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPositionText: String = "00:00",
    val errorMessage: String? = null
)
