package com.lalilu.lplaylist.repository

import com.lalilu.common.kv.KVContext
import com.lalilu.lplaylist.entity.LPlaylist

object PlaylistKV : KVContext("playlist") {

    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST")
        .apply { disableAutoSave() }
}
