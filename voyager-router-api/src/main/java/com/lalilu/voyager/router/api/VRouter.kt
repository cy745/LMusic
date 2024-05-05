package com.lalilu.voyager.router.api


object VRouter {
    private val routeSet = LinkedHashSet<RouteItem>()

    init {
        init()
    }

    fun build(url: String): RouteRequest = RouteRequest(
        baseUrl = url,
        routeSet = routeSet
    )

    fun register(routeItem: RouteItem) {
        routeSet.add(routeItem)
    }

    private fun init() {
        val vRouter = runCatching {
            Class.forName("com.lalilu.voyager.router.api.VRouter\$Routes")
                ?.getDeclaredField("INSTANCE")
                ?.get(null)
        }.getOrElse {
            it.printStackTrace()
            null
        }

        println("vRouter: ${vRouter != null}")
    }
}
