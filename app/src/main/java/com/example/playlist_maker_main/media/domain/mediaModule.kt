package com.example.playlist_maker_main.media.domain

import com.example.playlist_maker_main.media.ui.MediaViewModel
import com.example.playlist_maker_main.media.ui.favorites.FavoritesViewModel
import com.example.playlist_maker_main.media.ui.playlists.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel { MediaViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { PlaylistsViewModel() }
}
