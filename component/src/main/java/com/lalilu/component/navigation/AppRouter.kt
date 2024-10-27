package com.lalilu.component.navigation

import android.util.Log
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.component.base.TabScreen
import com.zhangke.krouter.KRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

sealed interface NavIntent {
    data class Jump(val screen: Screen) : NavIntent
    data class Push(val screen: Screen) : NavIntent
    data class Replace(val screen: Screen) : NavIntent
    data object Pop : NavIntent
    data object None : NavIntent
}

fun interface NavInterceptor {
    fun intercept(navigator: Navigator, intent: NavIntent): NavIntent
}

fun interface NavHandler {
    fun handle(navigator: Navigator, intent: NavIntent)
}

/**
 * 针对TabScreen的拦截处理逻辑
 */
val DefaultInterceptorForTabScreen = NavInterceptor { navigator, intent ->
    val screen = when (intent) {
        is NavIntent.Jump -> intent.screen
        is NavIntent.Push -> intent.screen
        is NavIntent.Replace -> intent.screen
        else -> return@NavInterceptor intent
    }

    if (screen !is TabScreen) {
        return@NavInterceptor intent
    }

    navigator.popUntilRoot()

    // 如果栈顶的页面与目标页面不同则替换
    if (navigator.lastItemOrNull != screen) {
        NavIntent.Push(screen)
    } else {
        NavIntent.None
    }
}

val DefaultHandler = NavHandler { navigator, intent ->
    when (intent) {
        NavIntent.Pop -> navigator.pop()
        is NavIntent.Push -> navigator.push(intent.screen)
        is NavIntent.Replace -> navigator.replace(intent.screen)
        is NavIntent.Jump -> navigator.push(intent.screen)
        NavIntent.None -> {}
    }
}

object AppRouter : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val sharedFlow = MutableSharedFlow<NavIntent>()
    private var handler: NavHandler = DefaultHandler
    private val interceptors = mutableListOf(
        DefaultInterceptorForTabScreen,
    )

    suspend fun bind(
        navigator: Navigator,
        onHandler: () -> Unit = {}
    ): Unit = sharedFlow.collect { intent ->
        interceptors
            .fold(intent) { temp, interceptor -> interceptor.intercept(navigator, temp) }
            .let { handler.handle(navigator, it) }
        onHandler()
    }

    fun intent(intent: NavIntent) = launch {
        sharedFlow.emit(intent)
    }

    fun intent(block: AppRouter.() -> NavIntent?) = launch {
        this@AppRouter.block()?.let { sharedFlow.emit(it) }
    }

    fun route(baseUrl: String): Request = Request(baseUrl)

    class Request internal constructor(
        private val baseUrl: String,
        private val params: MutableMap<String, Any?> = mutableMapOf()
    ) {
        fun <T : Any?> with(key: String, value: T) = apply { params[key] = value }

        fun jump() = requestResult()?.let { intent(NavIntent.Jump(it)) }
        fun push() = requestResult()?.let { intent(NavIntent.Push(it)) }
        fun replace() = requestResult()?.let { intent(NavIntent.Replace(it)) }
        fun get() = requestResult()

        private fun requestResult(): Screen? =
            runCatching { KRouter.route<Screen>(baseUrl, params) }
                .getOrElse {
                    Log.e("AppRouter", "route request for [$baseUrl] Failed", it)
                    null
                }
    }
}