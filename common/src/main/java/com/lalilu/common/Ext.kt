package com.lalilu.common

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope

private val handler by lazy { Handler(Looper.getMainLooper()) }
fun CoroutineScope.post(block: () -> Unit) = handler.post(block)
