package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lalilu.lmusic.Config

class SettingsSp(context: Context) : BaseSp() {
    override val sp: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Application.MODE_PRIVATE)
    }

    val blockedPaths = stringSetSp(
        Config.KEY_SETTINGS_BLOCKED_PATHS
    )
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
    val lyricTypefacePath = stringSp(
        Config.KEY_SETTINGS_LYRIC_TYPEFACE_PATH,
        Config.DEFAULT_SETTINGS_LYRIC_TYPEFACE_PATH
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
    val darkModeOption = intSp(
        Config.KEY_SETTINGS_DARK_MODE_OPTION,
        Config.DEFAULT_SETTINGS_DARK_MODE_OPTION
    )
    val autoHideSeekbar = boolSp(
        Config.KEY_SETTINGS_AUTO_HIDE_SEEKBAR,
        Config.DEFAULT_SETTINGS_AUTO_HIDE_SEEKBAR
    )
    val forceHideStatusBar = boolSp(
        Config.KEY_SETTINGS_FORCE_HIDE_STATUS_BAR,
        Config.DEFAULT_SETTINGS_FORCE_HIDE_STATUS_BAR
    )
    val keepScreenOnWhenLyricExpanded = boolSp(
        Config.KEY_SETTINGS_KEEP_SCREEN_ON_WHEN_LYRIC_EXPANDED,
        Config.DEFAULT_SETTINGS_KEEP_SCREEN_ON_WHEN_LYRIC_EXPANDED
    )
    val durationFilter = intSp(
        Config.KEY_SETTINGS_DURATION_FILTER,
        Config.DEFAULT_SETTINGS_DURATION_FILTER
    )
    val isDrawTranslation = boolSp(
        Config.KEY_SETTINGS_IS_DRAW_TRANSLATION,
        Config.DEFAULT_SETTINGS_IS_DRAW_TRANSLATION
    )
    val isEnableBlurEffect = boolSp(
        Config.KEY_SETTINGS_IS_ENABLE_BLUR_EFFECT,
        Config.DEFAULT_SETTINGS_IS_ENABLE_BLUR_EFFECT
    )
    val isGuidingOver = boolSp(
        Config.KEY_REMEMBER_IS_GUIDING_OVER,
        false
    )
}