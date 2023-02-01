package com.lalilu.lmusic.utils.filter

import com.blankj.utilcode.util.FileUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter
import com.lalilu.lmusic.datastore.SettingsDataStore

class DictionaryFilter(
    private val settingsDataStore: () -> SettingsDataStore
) : IndexFilter {
    override fun onSongsBuilt(songs: List<LSong>): List<LSong> =
        settingsDataStore().run {
            val pathList = ignoreDictionaries.get()

            songs.filter {
                val path = FileUtils.getDirName(it.pathStr)
                    ?.takeIf(String::isNotEmpty)
                    ?: "Unknown dir"
                !pathList.contains(path)
            }
        }
}