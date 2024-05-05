package com.lalilu.voyager.router.api


data class RouteItem(
    val path: String,
    val remark: String? = null,
    val getScreen: ValueHolder.() -> RouteResult = { RouteResult.None },
)