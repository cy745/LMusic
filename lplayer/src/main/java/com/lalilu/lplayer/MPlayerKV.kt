package com.lalilu.lplayer

import com.lalilu.common.kv.KVContext

object MPlayerKV : KVContext("mplayer") {
    val historyPlaylistIds = obtainList<String>("history_playlist_ids")
    val handleAudioFocus = obtain<Boolean>("handleAudioFocus", true)
    val handleBecomeNoisy = obtain<Boolean>("handleBecomeNoisy", true)
    val historyPlayPosition = obtain<Long>("history_play_position")
    val autoPlayWhenRestart = obtain<Boolean>("auto_play_when_restart")

    val playMode = obtain<String>("play_mode")
}