package com.lalilu.lplayer

import com.lalilu.common.kv.BaseKV

object MPlayerKV : BaseKV(prefix = "mplayer") {
    val historyPlaylistIds = obtainList<String>("history_playlist_ids")
    val handleAudioFocus = obtain<Boolean>("handleAudioFocus")
    val handleBecomeNoisy = obtain<Boolean>("handleBecomeNoisy")
    val playMode = obtain<String>("play_mode")
}