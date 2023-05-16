package com.lalilu.lplayer

import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.playback.impl.LocalPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object LPlayer {

    const val ACTION_SET_REPEAT_MODE = "ACTION_SET_REPEAT_MODE"

    const val MEDIA_DEFAULT_ACTION = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_SET_REPEAT_MODE or
            PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE

    val module = module {
        single { LocalPlayer(androidContext()) }
        single { AudioFocusHelper(androidContext()) }
    }
}