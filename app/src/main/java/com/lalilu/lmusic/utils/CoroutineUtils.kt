package com.lalilu.lmusic.utils

import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.cancellation.CancellationException

fun CoroutineScope.safeLaunch(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(dispatcher) {
        try {
            block()
        } catch (ce: CancellationException) {
            Log.e("[CancellationException]", "Coroutine error", ce)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("[Exception]", "Coroutine error", e)
        }
    }
}