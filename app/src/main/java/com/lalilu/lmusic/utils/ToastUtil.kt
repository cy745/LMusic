package com.lalilu.lmusic.utils

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class ToastUtil @Inject constructor(
    @ApplicationContext val mContext: Context
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var lastToast: Pair<String, Toast>? = null

    fun show(text: String?) = launch(Dispatchers.IO) {
        if (text == null) {
            cancelToast(lastToast)
            return@launch
        }

        lastToast = if (lastToast == null) {
            createToast(text)
        } else {
            if (text != lastToast?.first) {
                cancelToast(lastToast)
                createToast(text)
            } else {
                null
            }
        }

        showToast(lastToast)
    }

    private suspend fun createToast(text: String): Pair<String, Toast> =
        withContext(Dispatchers.Main) {
            return@withContext Pair(text, Toast.makeText(mContext, text, Toast.LENGTH_SHORT))
        }

    private suspend fun cancelToast(pair: Pair<String, Toast>?) =
        withContext(Dispatchers.Main) {
            pair?.second?.cancel()
        }

    private suspend fun showToast(pair: Pair<String, Toast>?) =
        withContext(Dispatchers.Main) {
            pair?.second?.show()
        }
}