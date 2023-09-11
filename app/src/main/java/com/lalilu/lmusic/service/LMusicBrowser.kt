package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lplayer.playback.Playback

class LMusicBrowser(
    private val context: Context,
    private val lastPlayedSp: LastPlayedSp,
    private val lMediaRepo: LMediaRepository,
    private val runtime: LMusicRuntime
) : DefaultLifecycleObserver {
    private var readyCallback: (() -> Unit)? = null
    private var controller: MediaControllerCompat? = null
    private val browser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context,
            ComponentName(context, LMusicService::class.java),
            ConnectionCallback(context),
            null
        )
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        setSongs(mediaIds = songs.map { it.id }, mediaId = song?.id)
    }

    fun setSongs(mediaIds: List<String>, mediaId: String? = null) {
        runtime.load(songs = mediaIds, playing = mediaId)
    }

    override fun onStart(owner: LifecycleOwner) {
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    fun whenConnected(callback: () -> Unit) {
        if (browser.isConnected && controller != null) {
            callback()
            return
        }
        readyCallback = callback
    }

    fun play() = controller?.transportControls?.play()
    fun pause() = controller?.transportControls?.pause()
    fun skipToNext() = controller?.transportControls?.skipToNext()
    fun skipToPrevious() = controller?.transportControls?.skipToPrevious()
    fun playById(id: String) = controller?.transportControls?.playFromMediaId(id, null)
    fun seekTo(position: Number) = controller?.transportControls?.seekTo(position.toLong())
    fun sendCustomAction(action: Playback.PlaybackAction) {
        controller?.transportControls?.sendCustomAction(action.name, null)
    }

    fun addToNext(mediaId: String): Boolean {
        val nowIndex = runtime.indexOfSong(mediaId = mediaId)
        val currentIndex = runtime.getPlayingIndex()
        if (currentIndex >= 0 && (currentIndex == nowIndex || (currentIndex + 1) == nowIndex))
            return false

        if (nowIndex >= 0) {
            runtime.move(nowIndex, currentIndex)
        } else {
            runtime.add(currentIndex + 1, mediaId)
        }
        return true
    }

    fun removeById(mediaId: String): Boolean {
        return try {
            if (mediaId == runtime.getPlayingId()) {
                runtime.getNextOf(runtime.getPlayingId(), true)?.let {
                    runtime.update(it)
                    playById(it)
                }
            }
            runtime.remove(mediaId)
            true
        } catch (e: Exception) {
            false
        }
    }

    private inner class ConnectionCallback(
        val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            controller = MediaControllerCompat(context, browser.sessionToken)

            // 若当前播放列表不为空，则不尝试提取历史数据填充
            if (!runtime.isEmpty()) {
                readyCallback?.invoke()
                readyCallback = null
                return
            }

            val songIds by lastPlayedSp.lastPlayedListIdsKey
            val lastPlayedIdKey by lastPlayedSp.lastPlayedIdKey

            if (songIds.isNotEmpty()) {
                LMedia.whenReady {
                    val songs = songIds.mapNotNull { lMediaRepo.requireSong(mediaId = it) }
                    val song = lMediaRepo.requireSong(mediaId = lastPlayedIdKey)
                    setSongs(songs, song)

                    readyCallback?.invoke()
                    readyCallback = null
                }
                return
            }


            LMedia.whenReady {
                val songs = lMediaRepo.getSongs()
                setSongs(songs, songs.getOrNull(0))

                readyCallback?.invoke()
                readyCallback = null
            }
        }
    }
}