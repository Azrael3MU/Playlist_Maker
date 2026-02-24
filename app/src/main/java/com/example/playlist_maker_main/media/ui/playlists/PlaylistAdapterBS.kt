package com.example.playlist_maker_main.media.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker_main.R
import com.example.playlist_maker_main.databinding.ItemPlaylistSmallBinding
import com.example.playlist_maker_main.media.domain.model.Playlist

class PlaylistAdapterBS(
    private val playlists: List<Playlist>,
    private val clickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapterBS.PlaylistViewHolderBS>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolderBS {
        val binding = ItemPlaylistSmallBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistViewHolderBS(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolderBS, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener { clickListener(playlists[position]) }
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistViewHolderBS(private val binding: ItemPlaylistSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.tvPlaylistNameSmall.text = playlist.name
            binding.tvTrackCountSmall.text = formatTrackCount(playlist.tracksCount)

            Glide.with(itemView)
                .load(playlist.imagePath)
                .placeholder(R.drawable.placeholder)
                .transform(CenterCrop(), RoundedCorners(2))
                .into(binding.ivPlaylistCoverSmall)
        }

        private fun formatTrackCount(count: Int): String {
            return itemView.context.resources.getQuantityString(R.plurals.track_count, count, count)
        }
    }
}