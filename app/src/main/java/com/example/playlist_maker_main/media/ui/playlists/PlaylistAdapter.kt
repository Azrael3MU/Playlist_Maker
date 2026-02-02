package com.example.playlist_maker_main.media.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.ItemPlaylistBinding
import com.example.playlist_maker_main.media.domain.model.Playlist

class PlaylistAdapter(private val playlists: List<Playlist>) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.tvPlaylistName.text = playlist.name
            binding.tvTrackCount.text = formatTrackCount(playlist.tracksCount)

            Glide.with(itemView)
                .load(playlist.imagePath)
                .placeholder(R.drawable.player_placeholder)
                .transform(CenterCrop(), RoundedCorners(8))
                .into(binding.ivPlaylistCover)
        }

        private fun formatTrackCount(count: Int): String {
            val rest10 = count % 10
            val rest100 = count % 100
            val word = when {
                rest100 in 11..19 -> "треков"
                rest10 == 1 -> "трек"
                rest10 in 2..4 -> "трека"
                else -> "треков"
            }
            return "$count $word"
        }
    }
}