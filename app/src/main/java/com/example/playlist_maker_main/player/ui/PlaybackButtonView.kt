package com.example.playlist_maker_main.player.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.playlist_maker_main.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null

    private var isPlaying = false
    private val imageRect = RectF()

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            0, 0
        ).apply {
            try {
                val playResId = getResourceId(R.styleable.PlaybackButtonView_imagePlay, 0)
                val pauseResId = getResourceId(R.styleable.PlaybackButtonView_imagePause, 0)

                if (playResId != 0) {
                    playBitmap = ResourcesCompat.getDrawable(resources, playResId, context.theme)?.toBitmap()
                }
                if (pauseResId != 0) {
                    pauseBitmap = ResourcesCompat.getDrawable(resources, pauseResId, context.theme)?.toBitmap()
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmapToDraw = if (isPlaying) pauseBitmap else playBitmap
        if (bitmapToDraw != null) {
            canvas.drawBitmap(bitmapToDraw, null, imageRect, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                isPlaying = !isPlaying
                invalidate()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setState(isPlaying: Boolean) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying
            invalidate()
        }
    }
}