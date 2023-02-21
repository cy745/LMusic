package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.lmusic.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LMusicSp @Inject constructor(
    @ApplicationContext context: Context
) : BaseSp() {
    override val sp: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Application.MODE_PRIVATE)
    }

    val blockedPaths = stringSetSp("BLOCKED_PATHS")

    val dayOfYear = intSp("DAY_OF_YEAR")
    val dailyRecommends = stringListSp("DAILY_RECOMMENDS")

    val lastPlayedIdKey = stringSp(Config.LAST_PLAYED_ID)
    val lastPlayedPositionKey = longSp(Config.LAST_PLAYED_POSITION)
    val lastPlayedListIdsKey = stringListSp(Config.LAST_PLAYED_LIST)

    val playMode = intSp(
        Config.KEY_SETTINGS_PLAY_MODE,
        Config.DEFAULT_SETTINGS_PLAY_MODE
    )
    val lyricTextSize = intSp(
        Config.KEY_SETTINGS_LYRIC_TEXT_SIZE,
        Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
    )
    val lyricGravity = intSp(
        Config.KEY_SETTINGS_LYRIC_GRAVITY,
        Config.DEFAULT_SETTINGS_LYRIC_GRAVITY
    )
    val seekBarHandler = intSp(
        Config.KEY_SETTINGS_SEEKBAR_HANDLER,
        Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
    )
    val ignoreAudioFocus = boolSp(
        Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS,
        Config.DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS
    )
    val volumeControl = intSp(
        Config.KEY_SETTINGS_VOLUME_CONTROL,
        Config.DEFAULT_SETTINGS_VOLUME_CONTROL
    )
    val lyricTypefaceUri = stringSp(
        Config.KEY_SETTINGS_LYRIC_TYPEFACE_URI,
        Config.DEFAULT_SETTINGS_LYRIC_TYPEFACE_URI
    )
    val enableStatusLyric = boolSp(
        Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE,
        Config.DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE
    )
    val enableSystemEq = boolSp(
        Config.KEY_SETTINGS_ENABLE_SYSTEM_EQ,
        Config.DEFAULT_SETTINGS_ENABLE_SYSTEM_EQ
    )
    val enableDynamicTips = boolSp(
        Config.KEY_SETTINGS_ENABLE_DYNAMIC_TIPS,
        Config.DEFAULT_SETTINGS_ENABLE_DYNAMIC_TIPS
    )
    val enableUnknownFilter = boolSp(
        Config.KEY_SETTINGS_ENABLE_UNKNOWN_FILTER,
        Config.DEFAULT_SETTINGS_ENABLE_UNKNOWN_FILTER
    )
    val ignoreDictionaries = stringListSp(
        Config.KEY_SETTINGS_IGNORE_DICTIONARIES,
        Config.DEFAULT_SETTINGS_IGNORE_DICTIONARIES
    )
    val autoHideSeekbar = boolSp(
        Config.KEY_SETTINGS_AUTO_HIDE_SEEKBAR,
        Config.DEFAULT_SETTINGS_AUTO_HIDE_SEEKBAR
    )


    val isGuidingOver = boolSp(Config.KEY_REMEMBER_IS_GUIDING_OVER, false)
}