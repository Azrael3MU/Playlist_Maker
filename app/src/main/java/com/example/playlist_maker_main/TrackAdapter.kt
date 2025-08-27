package com.example.playlist_maker_main

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.doOnLayout
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
        val dot: ImageView     = v.findViewById(R.id.dot_view)
        val metaRow: View      = v.findViewById(R.id.meta_row)
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
        h.time.text = t.durationStr()

        h.metaRow.doOnLayout {
            val rowW = h.metaRow.width - h.metaRow.paddingLeft - h.metaRow.paddingRight

            val lpDot  = h.dot.layoutParams  as ViewGroup.MarginLayoutParams
            val lpTime = h.time.layoutParams as ViewGroup.MarginLayoutParams

            val timeText = h.time.text.toString()
            val timeW = (h.time.paint.measureText(timeText) + 0.5f).toInt()

            val dotW = (h.dot.drawable?.intrinsicWidth ?: dpToPx(h.itemView, 4))

            val occupied = timeW + lpTime.leftMargin + lpTime.rightMargin +
                    dotW   + lpDot.leftMargin  + lpDot.rightMargin

            val avail = (rowW - occupied).coerceAtLeast(0)

            val lpArtist = h.artist.layoutParams
            if (lpArtist.width != avail) {
                lpArtist.width = avail
                h.artist.layoutParams = lpArtist
            }
            h.artist.text = TextUtils.ellipsize(
                t.artistName,
                h.artist.paint,
                avail.toFloat(),
                TextUtils.TruncateAt.END
            )
        }

        val radius = h.itemView.context.resources.getDimensionPixelSize(R.dimen.corner_radius)
        Glide.with(h.itemView)
            .load(t.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(radius))
            .into(h.artwork)

        h.itemView.setOnClickListener { onClick(t) }
    }
    private fun dpToPx(view: View, dp: Int): Int {
        val d = view.resources.displayMetrics.density
        return (dp * d + 0.5f).toInt()
    }

    override fun getItemCount() = tracks.size
}
