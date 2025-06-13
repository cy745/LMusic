package com.lalilu.common.kv.impl

import com.lalilu.common.kv.KVConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class KVJsonConverter<T>(
    val json: Json,
    val clazzQualifiedName: String,
    val serializer: KSerializer<T>
) : KVConverter {
    @Suppress("UNCHECKED_CAST")
    override fun convert(value: Any?): String {
        return json.encodeToString(serializer, value as T)
    }

    override fun restore(content: String): Any? {
        return json.decodeFromString<T>(serializer, content)
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override fun accept(clazz: KClass<*>, default: Any?, payload: String): Boolean {
        return clazzQualifiedName == payload
    }
}
