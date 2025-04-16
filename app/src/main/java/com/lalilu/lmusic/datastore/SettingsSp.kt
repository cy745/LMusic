package com.lalilu.lmusic.datastore

import android.app.Application
import android.content.SharedPreferences
import com.lalilu.common.base.BaseSp
import com.lalilu.lmusic.Config

class SettingsSp(private val context: Application) : BaseSp() {
    override fun obtainSourceSp(): SharedPreferences {
        return context.getSharedPreferences(context.packageName, Application.MODE_PRIVATE)
    }

    val playMode = obtain<Int>(
        Config.KEY_SETTINGS_PLAY_MODE,
        Config.DEFAULT_SETTINGS_PLAY_MODE
    )
    val lyricTextSize = obtain<Int>(
        Config.KEY_SETTINGS_LYRIC_TEXT_SIZE,
        Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
    )
    val lyricGravity = obtain<Int>(
        Config.KEY_SETTINGS_LYRIC_GRAVITY,
        Config.DEFAULT_SETTINGS_LYRIC_GRAVITY
    )
    val ignoreAudioFocus = obtain<Boolean>(
        Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS,
        Config.DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS
    )
    val volumeControl = obtain<Int>(
        Config.KEY_SETTINGS_VOLUME_CONTROL,
        Config.DEFAULT_SETTINGS_VOLUME_CONTROL
    )
    val lyricTypefacePath = obtain<String>(
        Config.KEY_SETTINGS_LYRIC_TYPEFACE_PATH,
        Config.DEFAULT_SETTINGS_LYRIC_TYPEFACE_PATH
    )
    val enableStatusLyric = obtain<Boolean>(
        Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE,
        Config.DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE
    )
    val enableSystemEq = obtain<Boolean>(
        Config.KEY_SETTINGS_ENABLE_SYSTEM_EQ,
        Config.DEFAULT_SETTINGS_ENABLE_SYSTEM_EQ
    )
    val enableDynamicTips = obtain<Boolean>(
        Config.KEY_SETTINGS_ENABLE_DYNAMIC_TIPS,
        Config.DEFAULT_SETTINGS_ENABLE_DYNAMIC_TIPS
    )
    val enableUnknownFilter = obtain<Boolean>(
        Config.KEY_SETTINGS_ENABLE_UNKNOWN_FILTER,
        Config.DEFAULT_SETTINGS_ENABLE_UNKNOWN_FILTER
    )
    val darkModeOption = obtain<Int>(
        Config.KEY_SETTINGS_DARK_MODE_OPTION,
        Config.DEFAULT_SETTINGS_DARK_MODE_OPTION
    )
    val autoHideSeekbar = obtain<Boolean>(
        Config.KEY_SETTINGS_AUTO_HIDE_SEEKBAR,
        Config.DEFAULT_SETTINGS_AUTO_HIDE_SEEKBAR
    )
    val forceHideStatusBar = obtain<Boolean>(
        Config.KEY_SETTINGS_FORCE_HIDE_STATUS_BAR,
        Config.DEFAULT_SETTINGS_FORCE_HIDE_STATUS_BAR
    )
    val keepScreenOnWhenLyricExpanded = obtain<Boolean>(
        Config.KEY_SETTINGS_KEEP_SCREEN_ON_WHEN_LYRIC_EXPANDED,
        Config.DEFAULT_SETTINGS_KEEP_SCREEN_ON_WHEN_LYRIC_EXPANDED
    )
    val durationFilter = obtain<Int>(
        Config.KEY_SETTINGS_DURATION_FILTER,
        Config.DEFAULT_SETTINGS_DURATION_FILTER
    )
    val isDrawTranslation = obtain<Boolean>(
        Config.KEY_SETTINGS_IS_DRAW_TRANSLATION,
        Config.DEFAULT_SETTINGS_IS_DRAW_TRANSLATION
    )
    val isEnableBlurEffect = obtain<Boolean>(
        Config.KEY_SETTINGS_IS_ENABLE_BLUR_EFFECT,
        Config.DEFAULT_SETTINGS_IS_ENABLE_BLUR_EFFECT
    )
    val isGuidingOver = obtain<Boolean>(
        Config.KEY_REMEMBER_IS_GUIDING_OVER,
        false
    )
}