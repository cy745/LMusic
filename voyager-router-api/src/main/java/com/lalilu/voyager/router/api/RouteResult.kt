package com.lalilu.voyager.router.api

import cafe.adriel.voyager.core.screen.Screen

sealed class RouteResult {
    data class Success(val screen: Screen) : RouteResult()
    data class Error(val message: String) : RouteResult()
    data object None : RouteResult()
}