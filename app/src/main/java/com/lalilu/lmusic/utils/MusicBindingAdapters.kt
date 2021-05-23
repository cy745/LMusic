package com.lalilu.lmusic.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lalilu.lmusic.R

class MusicBindingAdapters {
    companion object {
        @BindingAdapter("app:iconRec")
        @JvmStatic
        fun setIcon(imageView: ImageView, string: String) {
            println(string.uppercase())
            if (string.uppercase().contains("FLAC")) {
                imageView.setImageResource(R.drawable.ic_flac)
            }
            if (string.uppercase().contains("WAV")) {
                imageView.setImageResource(R.drawable.ic_wav)
            }
            if (string.uppercase().contains("MPEG")) {
                imageView.setImageResource(R.drawable.ic_mp3)
            }
            if (string.uppercase().contains("APE")) {
                imageView.setImageResource(R.drawable.ic_ape)
            }
        }
    }
}