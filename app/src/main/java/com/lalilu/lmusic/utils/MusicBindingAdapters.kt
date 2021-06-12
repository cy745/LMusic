package com.lalilu.lmusic.utils

import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.lalilu.lmusic.R
import com.lalilu.lmusic.service2.MusicService.Companion.Song_Type

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
            if (string.uppercase().contains("APE")) {
                imageView.setImageResource(R.drawable.ic_ape)
            }
            if (string.uppercase().contains("MP3")) {
                imageView.setImageResource(R.drawable.ic_mp3)
            }
        }

        @BindingAdapter("app:pictureUri")
        @JvmStatic
        fun setPictureUri(simpleDraweeView: SimpleDraweeView?, uri: Uri?) {
            simpleDraweeView?.setImageURI(uri)
        }

        fun getDurationFromExtra(extra: Bundle?): String {
            return DurationUtils.durationToString(
                extra?.getLong(
                    MediaMetadata.METADATA_KEY_DURATION,
                    0
                ) ?: 0
            )
        }

        fun getArtistFromExtra(extra: Bundle?): String {
            return extra?.getString(
                MediaMetadata.METADATA_KEY_ARTIST,
                "someone..."
            ) ?: "someone..."
        }

        fun getSongTypeFromExtra(extra: Bundle?): String {
            return extra?.getString(Song_Type, "mp3") ?: "mp3"
        }
    }
}