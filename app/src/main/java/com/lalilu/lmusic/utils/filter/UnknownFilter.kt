package com.lalilu.lmusic.utils.filter

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter

class UnknownFilter(
) : IndexFilter {
    override fun onSongsBuilt(songs: List<LSong>): List<LSong> =
        songs.filter {

            !it._artist.contains("<unknown>")
        }
}