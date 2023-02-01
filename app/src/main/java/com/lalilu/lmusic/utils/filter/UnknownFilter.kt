package com.lalilu.lmusic.utils.filter

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter
import com.lalilu.lmusic.datastore.SettingsDataStore

class UnknownFilter(
    private val settingsDataStore: () -> SettingsDataStore
) : IndexFilter {
    override fun onSongsBuilt(songs: List<LSong>): List<LSong> =
        songs.filter {
            if (settingsDataStore().run { enableUnknownFilter.get() } != true)
                return@filter true

            !it._artist.contains("<unknown>")
        }
}