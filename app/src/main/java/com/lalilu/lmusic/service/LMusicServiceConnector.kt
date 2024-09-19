package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.lalilu.lplayer.service.MService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LMusicServiceConnector(
    private val context: Context,
) : DefaultLifecycleObserver, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val sessionToken by lazy {
        SessionToken(context, ComponentName(context, MService::class.java))
    }
    private val browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()

    init {
        launch(Dispatchers.Main) {
            val browser = browserFuture.await()
            val item = browser.getItem("1000004055")
                .await()
                .value

            item?.let {
                browser.setMediaItem(item)
                browser.prepare()
                browser.play()
            }
        }
    }
}