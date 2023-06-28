package com.lalilu.lmusic.adapter

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import coil.load
import coil.util.CoilUtils.dispose
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.ui.BlurImageView

fun BlurImageView.loadCover(song: LSong?) {
    song ?: run {
        clearImage()
        return
    }
    val samplingTo = width

    load(song) {
        if (samplingTo > 0) size(samplingTo)
        allowHardware(false)
        target(
            onSuccess = { loadImageFromDrawable(it) },
            onError = { clearImage() }
        ).build()
    }
}

fun AppCompatImageView.loadCoverForPlaying(song: LSong?) {
    song ?: run {
        setImageDrawable(null)
        return
    }
    val samplingTo = width

    dispose(this)
    load(song) {
        if (samplingTo > 0) size(samplingTo)
        placeholder(R.drawable.ic_music_line_bg_64dp)
        error(R.drawable.ic_music_line_bg_64dp)
    }
}

fun AppCompatImageView.setRoundOutline(radius: Number) {
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0, 0, view.width, view.height,
                SizeUtils.dp2px(radius.toFloat()).toFloat()
            )
        }
    }
    clipToOutline = true
}