package com.lalilu.lplaylist.repository

import com.lalilu.common.kv.BaseKV
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.entity.LPlaylistFastEncoder
import io.fastkv.FastKV

class PlaylistKV(override val fastKV: FastKV) : BaseKV() {
    val playlistList = obtainList<LPlaylist>(key = "PLAYLIST", LPlaylistFastEncoder)
        .apply { disableAutoSave() }
}