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

typealias NavParams = Map<String, Any?>
typealias MutableNavParams = MutableMap<String, Any?>

sealed class NavIntent(
    open val screen: Screen? = null,
    open val params: NavParams = emptyMap()
) {
    data class Jump(
        override val screen: Screen,
        override val params: NavParams = emptyMap()
    ) : NavIntent(screen, params)

    data class Push(
        override val screen: Screen,
        override val params: NavParams = emptyMap()
    ) : NavIntent(screen, params)

    data class Replace(
        override val screen: Screen,
        override val params: NavParams = emptyMap()
    ) : NavIntent(screen, params)

    data class PopUtil(
        override val screen: Screen,
        override val params: NavParams = emptyMap()
    ) : NavIntent(screen, params)

    data object Pop : NavIntent()
    data object None : NavIntent()

    companion object {
        const val SINGLE_TOP = "singleTop"
        const val SINGLE_INSTANCE = "singleInstance"
    }
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

/**
 * 当意图中有 [NavIntent.SINGLE_TOP] 时，当前显示页面如果和目标页面相同则不执行跳转操作
 */
val DefaultSingleTopInterceptor = NavInterceptor { navigator, intent ->
    val screen = intent.screen ?: return@NavInterceptor intent
    val singleTop = intent.params[NavIntent.SINGLE_TOP] as? Boolean ?: return@NavInterceptor intent
    if (!singleTop) return@NavInterceptor intent

    if (navigator.lastItemOrNull?.key == screen.key) {
        return@NavInterceptor NavIntent.None
    }

    return@NavInterceptor intent
}

/**
 * 当意图中有 [NavIntent.SINGLE_INSTANCE] 时，当前页面和目标页面属于同类型则替换
 */
val DefaultSingleInstanceInterceptor = NavInterceptor { navigator, intent ->
    val lastScreen = navigator.lastItemOrNull ?: return@NavInterceptor intent
    val screen = intent.screen ?: return@NavInterceptor intent
    val singleInstance = intent.params[NavIntent.SINGLE_INSTANCE] as? Boolean
        ?: return@NavInterceptor intent
    if (!singleInstance) return@NavInterceptor intent

    if (lastScreen::class == screen::class) {
        return@NavInterceptor NavIntent.Replace(screen)
    }

    return@NavInterceptor intent
}

val DefaultHandler = NavHandler { navigator, intent ->
    when (intent) {
        NavIntent.Pop -> navigator.pop()
        is NavIntent.Push -> navigator.push(intent.screen)
        is NavIntent.Replace -> navigator.replace(intent.screen)
        is NavIntent.Jump -> navigator.push(intent.screen)
        is NavIntent.PopUtil -> navigator.popUntil { intent.screen == it }
        NavIntent.None -> {}
    }
}

object AppRouter : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val sharedFlow = MutableSharedFlow<NavIntent>()
    private var handler: NavHandler = DefaultHandler
    private val interceptors = mutableListOf(
        DefaultSingleTopInterceptor,
        DefaultSingleInstanceInterceptor,
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
        private val params: MutableNavParams = mutableMapOf()
    ) {
        fun <T : Any?> with(key: String, value: T) = apply { params[key] = value }
        fun withSingleInstance(singleInstance: Boolean = true) =
            apply { params[NavIntent.SINGLE_INSTANCE] = singleInstance }

        fun withSingleTop(singleTop: Boolean = true) =
            apply { params[NavIntent.SINGLE_TOP] = singleTop }

        fun jump() = requestResult()?.let { intent(NavIntent.Jump(it, params)) }
        fun push() = requestResult()?.let { intent(NavIntent.Push(it, params)) }
        fun replace() = requestResult()?.let { intent(NavIntent.Replace(it, params)) }
        fun get() = requestResult()

        private fun requestResult(): Screen? =
            runCatching { KRouter.route<Screen>(baseUrl, params) }
                .getOrElse {
                    Log.e("AppRouter", "route request for [$baseUrl] Failed", it)
                    null
                }
    }
}