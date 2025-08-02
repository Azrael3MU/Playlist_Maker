package com.example.playlist_maker_main

import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackAdapter(private val tracks: List<Track>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val artwork: ImageView = view.findViewById(R.id.artwork)
        val trackName: TextView = view.findViewById(R.id.track_name)
        val artistName: TextView = view.findViewById(R.id.artist_name)
        val trackTime: TextView = view.findViewById(R.id.track_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.trackName.text = track.trackName
        holder.artistName.text = track.artistName
        holder.trackTime.text = track.trackTime

        val radiusInPx = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.corner_radius)

        Glide.with(holder.itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .transform(RoundedCorners(radiusInPx))
            .centerCrop()
            .into(holder.artwork)
    }

    override fun getItemCount(): Int = tracks.size
}