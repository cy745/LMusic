package com.lalilu.lmusic

import android.support.v4.media.session.PlaybackStateCompat

object Config {
    const val BASE_NETEASE_URL = "https://apis.lalilu.cn/"
    const val BASE_KUGOU_SONGS_URL = "https://mobilecdn.kugou.com/"
    const val BASE_KUGOU_LYRIC_URL = "https://krcs.kugou.com/"

    const val LAST_PLAYED_SP = "LAST_PLAYED_SP"
    const val LAST_PLAYED_ID = "LAST_PLAYED_ID"
    const val LAST_PLAYED_LIST = "LAST_PLAYED_LIST"
    const val LAST_PLAYED_POSITION = "LAST_PLAYED_POSITION"

    const val ACTION_PLAY_AND_PAUSE = "action_play_and_pause"
    const val ACTION_RELOAD_AND_PLAY = "action_reload_and_play"
    const val ACTION_SET_REPEAT_MODE = "action_set_repeat_mode"

    const val KEY_SETTINGS_MEDIA_UNKNOWN_FILTER = "KEY_SETTINGS_MEDIA_UNKNOWN_FILTER"
    const val KEY_SETTINGS_KANHIRA_ENABLE = "KEY_SETTINGS_KANHIRA_ENABLE"
    const val KEY_SETTINGS_LYRIC_GRAVITY = "KEY_SETTINGS_LYRIC_GRAVITY"
    const val KEY_SETTINGS_LYRIC_TEXT_SIZE = "KEY_SETTINGS_LYRIC_TEXT_SIZE"
    const val KEY_SETTINGS_SEEKBAR_HANDLER = "KEY_SETTINGS_SEEKBAR_HANDLER"
    const val KEY_SETTINGS_STATUS_LYRIC_ENABLE = "KEY_SETTINGS_STATUS_LYRIC_ENABLE"
    const val KEY_SETTINGS_IGNORE_AUDIO_FOCUS = "KEY_SETTINGS_IGNORE_AUDIO_FOCUS"
    const val KEY_SETTINGS_VOLUME_CONTROL = "KEY_SETTINGS_VOLUME_CONTROL"
    const val KEY_SETTINGS_LYRIC_TYPEFACE_URI = "KEY_SETTINGS_LYRIC_TYPEFACE_URI"
    const val KEY_SETTINGS_ENABLE_SYSTEM_EQ = "KEY_SETTINGS_ENABLE_SYSTEM_EQ"
    const val KEY_SETTINGS_PLAY_MODE = "KEY_SETTINGS_PLAY_MODE"

    const val KEY_REMEMBER_IS_GUIDING_OVER = "KEY_REMEMBER_IS_GUIDING_OVER"

    const val DEFAULT_SETTINGS_MEDIA_UNKNOWN_FILTER = true
    const val DEFAULT_SETTINGS_KANHIRA_ENABLE = false
    const val DEFAULT_SETTINGS_LYRIC_GRAVITY = 1
    const val DEFAULT_SETTINGS_LYRIC_TEXT_SIZE = 16
    const val DEFAULT_SETTINGS_SEEKBAR_HANDLER = 0
    const val DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE = false
    const val DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS = false
    const val DEFAULT_SETTINGS_VOLUME_CONTROL = 100
    const val DEFAULT_SETTINGS_LYRIC_TYPEFACE_URI = ""
    const val DEFAULT_SETTINGS_ENABLE_SYSTEM_EQ = false
    const val DEFAULT_SETTINGS_PLAY_MODE = 0

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

    val MEDIA_STOPPED_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
        .build()
}