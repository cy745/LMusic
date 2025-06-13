package com.lalilu.lmusic.compose.screen.playing.lyric

import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVItem
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Named("LyricSettings")
@Single(createdAtStart = true)
fun provideLyricSettingsState(): KVItem<LyricSettings> {
    return KVContext.obtainStatic<LyricSettings>(
        key = "LyricSettings",
        defaultValue = LyricSettings()
    ).apply { disableAutoSave() }
}
