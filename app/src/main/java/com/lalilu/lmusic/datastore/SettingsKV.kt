package com.lalilu.lmusic.datastore

import com.lalilu.common.kv.KVContext
import com.lalilu.lmusic.Config

object SettingsKV : KVContext("settings") {
    val playMode = obtain<Int>(
        Config.KEY_SETTINGS_PLAY_MODE,
        Config.DEFAULT_SETTINGS_PLAY_MODE
    )
    val enableStatusLyric = obtain<Boolean>("enable_status_lyric", false)
    val enableSystemEq = obtain<Boolean>("enable_system_eq", false)
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
    ).apply { disableAutoSave() }
    val isGuidingOver = obtain<Boolean>(
        Config.KEY_REMEMBER_IS_GUIDING_OVER,
        false
    )
}