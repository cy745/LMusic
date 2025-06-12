package com.lalilu.lmusic.compose.screen.playing.lyric

import com.blankj.utilcode.util.LogUtils
import com.lalilu.common.kv.KVContext
import com.lalilu.common.kv.KVConverter
import com.lalilu.common.kv.KVItem
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Named("LyricSettings")
@Single(createdAtStart = true)
fun provideLyricSettingsState(
    json: Json
): KVItem<LyricSettings> {
    KVContext.registerConverter(object : KVConverter {
        override fun convert(value: Any?): String {
            return try {
                json.encodeToString(LyricSettings.serializer(), value as LyricSettings)
            } catch (e: Exception) {
                LogUtils.e(e)
                ""
            }
        }

        @Suppress("USELESS_CAST")
        override fun restore(content: String): Any? {
            return json.decodeFromString(LyricSettings.serializer(), content) as? LyricSettings
        }

        override fun accept(
            baseType: KClass<*>?,
            clazz: KClass<*>,
            default: Any?
        ): Boolean = clazz == LyricSettings::class && baseType == null
    })

    return KVContext.obtainStatic<LyricSettings>(
        key = "LyricSettings",
        defaultValue = LyricSettings()
    ).apply { disableAutoSave() }
}
