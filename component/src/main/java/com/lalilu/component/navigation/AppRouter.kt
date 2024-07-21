package com.lalilu.component.navigation

import cafe.adriel.voyager.core.screen.Screen
import com.zhangke.krouter.KRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

sealed interface NavIntent {
    data class Push(val screen: Screen) : NavIntent
    data class Replace(val screen: Screen) : NavIntent
    data object Pop : NavIntent
}

object AppRouter : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val channel = Channel<NavIntent>(
        capacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    val intentFlow = channel.receiveAsFlow()

    fun intent(intent: NavIntent) = launch {
        channel.send(intent)
    }

    fun intent(block: AppRouter.() -> NavIntent?) = launch {
        val i = this@AppRouter.block() ?: return@launch
        channel.send(i)
    }

    fun String.push(): NavIntent.Push? = KRouter
        .route<Screen>(this)
        ?.let(NavIntent::Push)

    fun String.replace(): NavIntent.Replace? = KRouter
        .route<Screen>(this)
        ?.let(NavIntent::Replace)
}