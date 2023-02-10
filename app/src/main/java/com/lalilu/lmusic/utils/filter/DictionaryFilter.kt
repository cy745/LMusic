package com.lalilu.lmusic.utils.filter

import com.blankj.utilcode.util.FileUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter
import com.lalilu.lmusic.datastore.BlockedSp
import com.lalilu.lmusic.datastore.SettingsDataStore

class DictionaryFilter(
    private val blockedSp: BlockedSp
) : IndexFilter {
    override fun onSongsBuilt(songs: List<LSong>): List<LSong> {
        val blockedPaths = blockedSp.blockedPaths.get() ?: emptyList()

        return songs.filter {
            val path = FileUtils.getDirName(it.pathStr)
                ?.takeIf(String::isNotEmpty)
                ?: "Unknown dir"
            !blockedPaths.contains(path)
        }
    }

}