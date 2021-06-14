package com.lalilu.lmusic.utils

import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.lalilu.lmusic.R
import com.lalilu.lmusic.service2.MusicService.Companion.SONG_TYPE

class MusicBindingAdapters {
    companion object {
        @BindingAdapter("app:iconRec")
        @JvmStatic
        fun setIcon(imageView: ImageView, string: String) {
            println("setIcon: $string")
            val strings = string.split("/")
            val result = when (strings[strings.size - 1].uppercase()) {
                "FLAC" -> R.drawable.ic_flac
                "WAV" -> R.drawable.ic_wav
                "X-WAV" -> R.drawable.ic_wav
                "APE" -> R.drawable.ic_ape
                "MPEG" -> R.drawable.ic_mp3
                "MP3" -> R.drawable.ic_mp3
                else -> R.drawable.ic_mp3
            }
            imageView.setImageResource(result)
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
            return extra?.getString(SONG_TYPE, "mp3") ?: "mp3"
        }
    }
}