package com.lalilu.common.kv.impl

import com.lalilu.common.kv.KVConverter
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

class StringListKVConverter : KVConverter {
    private val json by KoinPlatform.getKoin().inject<Json>()

    override fun convert(value: Any?): String {
        val list = (value as? List<*>)
            ?.mapNotNull { it as? String }
            ?: return ""

        return runCatching { json.encodeToString(list) }
            .getOrNull()
            ?: ""
    }

    override fun restore(content: String): Any? {
        return json.decodeFromString<List<String>>(content)
    }

    override fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean {
        return baseType == List::class && clazz == String::class
    }
}