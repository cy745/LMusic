package com.lalilu.lmusic

import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.PlaybackStateCompat

object Config {
    const val MEDIA_MIME_TYPE = "media_mime_type"

    const val LAST_METADATA = "LAST_METADATA"
    const val LAST_PLAYBACK_STATE = "LAST_PLAYBACK_STATE"
    const val LAST_PLAYLIST_ID = "LAST_PLAYLIST_ID"
    const val LAST_PLAYING_ID = "LAST_PLAYING_ID"
    const val LAST_POSITION = "LAST_POSITION"
    const val LAST_REPEAT_MODE = "LAST_REPEAT_MODE"

    const val LAST_LIST_ID = "LAST_LIST_ID"
    const val LAST_LIST_TYPE = "LAST_LIST_TYPE"

    const val SHARED_PLAYER = "SHARED_PLAYER"


    const val MEDIA_ID_EMPTY_ROOT = "media_id_empty_root"
    const val ACTION_PLAY_PAUSE = "play_and_pause"

    const val MEDIA_DEFAULT_ACTION = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_SET_REPEAT_MODE

    val MEDIA_STOPPED_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
        .build()

    val FILTER_BECOMING_NOISY = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
}