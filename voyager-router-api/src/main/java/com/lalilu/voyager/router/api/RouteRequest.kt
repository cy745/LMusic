package com.lalilu.voyager.router.api

@Suppress("UNCHECKED_CAST")
class ValueHolder {
    val typeMap by lazy {
        mutableMapOf<Class<out Any>, MutableMap<String, Any>>()
    }

    val listTypeMap by lazy {
        mutableMapOf<Class<out Any>, MutableMap<String, Any>>()
    }

    fun <T : Any> getMapByType(clazz: Class<T>): Map<String, T> {
        return typeMap.getOrElse(clazz) { emptyMap() } as Map<String, T>
    }

    fun <T : Any> getMapByListType(clazz: Class<T>): Map<String, List<T>> {
        return listTypeMap.getOrElse(clazz) { emptyMap() } as Map<String, List<T>>
    }
}

class RouteRequest internal constructor(
    private val baseUrl: String,
    private val routeSet: Set<RouteItem>
) {
    val valueHolder = ValueHolder()

    fun <T : Any> with(key: String, value: T) = apply {
        val map = valueHolder.typeMap.getOrPut(value::class.java) { mutableMapOf() }
        map[key] = value
    }

    inline fun <reified T : Any> with(key: String, value: List<T>) = apply {
        val map = valueHolder.listTypeMap.getOrPut(T::class.java) { mutableMapOf() }
        map[key] = value
    }

    fun go(): RouteResult {
        val route = routeSet.firstOrNull { it.path == baseUrl }
            ?: return RouteResult.Error("No route found for url: $baseUrl")

        return route.getScreen.invoke(valueHolder)
    }
}