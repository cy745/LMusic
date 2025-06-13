package com.lalilu.common.kv

import com.lalilu.common.kv.impl.KVJsonConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

interface KVConverter {
    fun convert(value: Any?): String
    fun restore(content: String): Any?
    fun accept(clazz: KClass<*>, default: Any?, payload: String): Boolean

    companion object {
        val converters = mutableListOf<KVConverter>()
        val typeExcludeToConvert = mutableListOf<KClass<*>>(
            String::class,
            Boolean::class,
            Int::class,
            Long::class,
            Float::class
        )

        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        inline fun <reified T : Any> findConverter(
            default: Any?,
            typeParameters: List<KClass<*>> = emptyList(),
        ): KVConverter? {
            // 如果类型在排除列表中，则不需要创建Converter
            if (T::class in typeExcludeToConvert) {
                return null
            }

            // 为每一个需要转换的类构建一个唯一的名称
            val paramsName = typeParameters.joinToString(",") { it.qualifiedName ?: "" }
            val clazzName = "${T::class.qualifiedName}<$paramsName>"
            val converter = converters
                .firstOrNull { it.accept(T::class, default, clazzName) }
                    as? KVConverter

            // 已经创建过对应类型的Converter，直接返回
            if (converter != null) {
                return converter
            }

            // 尝试获取可用的Serializer
            val json = KoinPlatform.getKoin().get<Json>()
            val serializer: KSerializer<*>? = serializer<T>(
                json = json,
                typeParameters = typeParameters,
            )

            // 确保Converter创建成功，若没有则跑出异常提示需要确保类都存在对应的Serializer
            requireNotNull(serializer) { "No KVConverter found for $clazzName" }

            return KVJsonConverter(json, clazzName, serializer)
                .also { converters += it }
        }

        @OptIn(InternalSerializationApi::class)
        inline fun <reified T : Any> serializer(
            json: Json,
            typeParameters: List<KClass<*>>,
        ): KSerializer<*>? {
            var serializer: KSerializer<*>? = json.serializersModule.serializerOrNull(typeOf<T>())
            if (serializer != null) return serializer

            if (typeParameters.isEmpty()) return null

            return when (T::class) {
                Array::class -> return ListSerializer(typeParameters[0].serializer())
                List::class -> return ListSerializer(typeParameters[0].serializer())
                Set::class -> return SetSerializer(typeParameters[0].serializer())
                Map::class -> return MapSerializer(
                    typeParameters[0].serializer(),
                    typeParameters[1].serializer()
                )

                else -> null
            }
        }
    }
}