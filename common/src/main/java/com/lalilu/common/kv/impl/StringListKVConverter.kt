package com.lalilu.common.kv.impl

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.lalilu.common.kv.KVConverter
import kotlin.reflect.KClass

class StringListKVConverter : KVConverter {
    val typeToken = object : TypeToken<List<String>>() {}

    override fun convert(value: Any?): String {
        val list = (value as? List<*>)
            ?.mapNotNull { it as? String }
            ?: return ""

        return GsonUtils.toJson(list, typeToken.type)
    }

    override fun restore(content: String): Any? {
        return GsonUtils.fromJson(content, typeToken.type)
    }

    override fun accept(baseType: KClass<*>?, clazz: KClass<*>, default: Any?): Boolean {
        return baseType == List::class && clazz == String::class
    }
}