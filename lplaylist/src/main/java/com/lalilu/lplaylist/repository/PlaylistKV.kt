package com.lalilu.lplaylist.repository

import com.lalilu.common.kv.BaseKV
import com.lalilu.lplaylist.entity.LPlaylist

object PlaylistKV : BaseKV() {
    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST")
        .apply { disableAutoSave() }
}