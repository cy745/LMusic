package com.lalilu.lmusic.compose.screen.playing.lyric

import com.funny.data_saver.core.DataSaverConverter
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.SavePolicy
import com.funny.data_saver.core.mutableDataSaverStateOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Named("LyricSettings")
@Single(createdAtStart = true)
fun provideLyricSettingsState(
    dataSaverInterface: DataSaverInterface,
    json: Json
): DataSaverMutableState<LyricSettings> {
    DataSaverConverter.registerTypeConverters<LyricSettings>(
        save = { json.encodeToString(it) },
        restore = { json.decodeFromString<LyricSettings>(it) }
    )

    return mutableDataSaverStateOf<LyricSettings>(
        dataSaverInterface = dataSaverInterface,
        key = "LyricSettings",
        initialValue = LyricSettings(),
        savePolicy = SavePolicy.NEVER
    )
}
