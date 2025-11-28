package com.example.playlist_maker_main.media.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlist_maker_main.media.ui.MediaFragment
import com.example.playlist_maker_main.media.ui.favorites.FavoritesFragment
import com.example.playlist_maker_main.media.ui.playlists.PlaylistsFragment

class MediaPagerAdapter(fragment: MediaFragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritesFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> FavoritesFragment.newInstance()
        }
    }
}