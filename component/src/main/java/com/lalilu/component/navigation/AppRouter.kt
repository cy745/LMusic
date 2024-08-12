package com.lalilu.component.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.zhangke.krouter.KRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

sealed interface NavIntent {
    data class Jump(val screen: Screen) : NavIntent
    data class Push(val screen: Screen) : NavIntent
    data class Replace(val screen: Screen) : NavIntent
    data object Pop : NavIntent
}

object AppRouter : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val sharedFlow = MutableSharedFlow<Pair<String, NavIntent>>()

    fun bindFor(baseUrl: String = "") = sharedFlow
        .filter { it.first == baseUrl }
        .map { it.second }

    @Deprecated("弃用")
    fun intent(intent: NavIntent) = launch {
        sharedFlow.emit("" to intent)
    }

    @Deprecated("弃用")
    fun intent(block: AppRouter.() -> NavIntent?) = launch {
        val i = this@AppRouter.block() ?: return@launch
        sharedFlow.emit("" to i)
    }

    fun intent(key: String, intent: NavIntent) = launch {
        sharedFlow.emit(key to intent)
    }

    fun route(baseUrl: String): Request = route("", baseUrl)
    fun route(key: String, baseUrl: String): Request {
        return Request(key, baseUrl)
    }

    class Request internal constructor(
        private val key: String,
        private val baseUrl: String,
        private val params: MutableMap<String, Any?> = mutableMapOf()
    ) {
        fun <T : Any?> with(key: String, value: T) = apply { params[key] = value }

        fun jump() = requestResult()?.let { intent(key, NavIntent.Jump(it)) }
        fun push() = requestResult()?.let { intent(key, NavIntent.Push(it)) }
        fun replace() = requestResult()?.let { intent(key, NavIntent.Replace(it)) }
        fun get() = requestResult()

        private fun requestResult(): Screen? =
            runCatching { KRouter.route<Screen>(baseUrl, params) }
                .getOrNull()
    }
}