package com.example.playlist_maker_main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackAdapter(
    private var tracks: List<Track>,
    private val onClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackVH>() {

    fun submitList(list: List<Track>) {
        tracks = list
        notifyDataSetChanged()
    }

    class TrackVH(v: View) : RecyclerView.ViewHolder(v) {
        val artwork: ImageView = v.findViewById(R.id.artwork)
        val name: TextView = v.findViewById(R.id.track_name)
        val artist: TextView = v.findViewById(R.id.artist_name)
        val time: TextView = v.findViewById(R.id.track_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackVH(view)
    }

    override fun onBindViewHolder(h: TrackVH, pos: Int) {
        val t = tracks[pos]
        h.artist.text = ""
        h.name.text = t.trackName
        h.artist.text = t.artistName
        h.artist.requestLayout()
        h.time.text = t.durationStr()

        val radius = h.itemView.context.resources.getDimensionPixelSize(R.dimen.corner_radius)
        Glide.with(h.itemView)
            .load(t.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(h.artwork)

        h.itemView.setOnClickListener { onClick(t) }
    }

    override fun getItemCount() = tracks.size
}
