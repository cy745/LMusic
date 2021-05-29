package com.lalilu.lmusic.utils

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.lalilu.lmusic.R

class MusicBindingAdapters {
    companion object {
        @BindingAdapter("app:iconRec")
        @JvmStatic
        fun setIcon(imageView: ImageView, string: String) {
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

        @BindingAdapter("app:pictureUri")
        @JvmStatic
        fun setPictureUri(simpleDraweeView: SimpleDraweeView, uri: Uri) {
            simpleDraweeView.setImageURI(uri)
        }
    }
}