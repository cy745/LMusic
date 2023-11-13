package com.lalilu.component

import com.lalilu.lmedia.extension.ListAction

interface Manager<T> {
    fun register(key: String, item: T)
    fun getAll(): List<T>
    fun get(key: String): T?
}

abstract class BaseManager<T> : Manager<T> {
    val map = linkedMapOf<String, T>()

    override fun register(key: String, item: T) {
        println("[BaseManager]register: $key $item")
        map[key] = item
    }

    override fun getAll(): List<T> {
        return map.values.toList()
    }

    override fun get(key: String): T? {
        return map[key]
    }

    inline fun <reified K : T> tryGet(key: String): K? {
        return map[key] as? K
    }
}

object SortRuleManager : BaseManager<ListAction>() {

}

