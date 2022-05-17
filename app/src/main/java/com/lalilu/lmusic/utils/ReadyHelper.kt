package com.lalilu.lmusic.utils

import androidx.annotation.IntDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

const val STATE_CREATED = 1
const val STATE_INITIALIZING = 2
const val STATE_INITIALIZED = 3
const val STATE_ERROR = 4

@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
@Retention(AnnotationRetention.SOURCE)
annotation class ReadyState

interface ReadyHelper {
    suspend fun start()

    /**
     * 当加载为完成时将回调函数保存，完成时直接执行回调函数
     *
     * @param forOnce 是否全局只需执行一次回调，执行完毕后移除该回调
     * @param performAction 需要执行的回调函数
     */
    fun whenReady(
        forOnce: Boolean = false,
        performAction: suspend (Boolean) -> Unit
    ): Boolean

    fun initialize()
    fun startSync()
}

abstract class BaseReadyHelper : ReadyHelper, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var readyCallbacks: LinkedHashMap<suspend (Boolean) -> Unit, Boolean> = LinkedHashMap()

    @ReadyState
    var readyState: Int = STATE_CREATED
        set(value) {
            if (field == value) return
            field = value
            when (value) {
                STATE_INITIALIZED,
                STATE_ERROR -> {
                    synchronized(readyCallbacks) {
                        launch {
                            readyCallbacks.forEach {
                                it.key.invoke(value != STATE_ERROR)
                                if (it.value) readyCallbacks.remove(it.key)
                            }
                        }
                    }
                }
            }
        }

    override fun whenReady(forOnce: Boolean, performAction: suspend (Boolean) -> Unit): Boolean {
        return when (readyState) {
            STATE_CREATED, STATE_INITIALIZING -> {
                readyCallbacks[performAction] = forOnce
                false
            }
            else -> {
                launch { performAction(readyState != STATE_ERROR) }
                true
            }
        }
    }

    override suspend fun start() {
        try {
            readyState = STATE_INITIALIZING
            initialize()
            readyState = STATE_INITIALIZED
        } catch (e: Exception) {
            readyState = STATE_ERROR
        }
    }

    override fun startSync() {
        launch { start() }
    }
}