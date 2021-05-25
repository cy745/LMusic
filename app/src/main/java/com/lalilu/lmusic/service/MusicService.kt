package com.lalilu.lmusic.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.lalilu.lmusic.MusicPlayer
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.NotificationUtils
import com.lalilu.lmusic.utils.cancelNotifications
import com.lalilu.lmusic.utils.sendNotification
import com.lalilu.lmusic.viewmodel.MusicServiceViewModel
import java.util.*
import kotlin.collections.ArrayList

class MusicService : Service(), LifecycleOwner {
    private val mBinder: MusicBinder = MusicBinder()
    private val musicPlayer: MusicPlayer = MusicPlayer(this)
    private val mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private lateinit var serviceViewModel: MusicServiceViewModel
    private lateinit var durationTimer: Timer
    private lateinit var notificationManager: NotificationManager

    inner class MusicBinder : Binder() {
        private var songList: List<Song> = ArrayList()
        private var playingSong: Song = Song()

        fun toggle() = musicPlayer.toggle()
        fun play() = musicPlayer.play()
        fun pause() = musicPlayer.pause()
        fun stop() {
            notificationManager.cancelNotifications()
            musicPlayer.stop()
        }

        fun next() {
            val index = songList.indexOf(playingSong)
            if (index < songList.size - 1) MusicServiceViewModel.getInstance().getPlayingSong()
                .postValue(songList[index + 1])
        }

        fun last() {
            val index = songList.indexOf(playingSong)
            if (index - 1 >= 0) MusicServiceViewModel.getInstance().getPlayingSong()
                .postValue(songList[index - 1])
        }

        fun setSong(song: Song) {
            notificationManager.sendNotification(
                song.songTitle,
                NotificationUtils.playerChannelName + "_ID",
                applicationContext
            )

            playingSong = song
            musicPlayer.setSong(song)
            musicPlayer.setOnCompletionListener {
                next()
            }
        }

        fun setSongList(songs: List<Song>) {
            songList = songs
        }

        fun setDuration(duration: Number) = musicPlayer.setDuration(duration)
        fun getDuration() = musicPlayer.getDuration()
    }

    private fun initObserver() {
        serviceViewModel.getPlayingSong().observe(this, {
            mBinder.setSong(it)
        })
        serviceViewModel.getSongList().observe(this, {
            mBinder.setSongList(it)
        })
        serviceViewModel.getPlayingDuration().observe(this, {
            mBinder.setDuration(it)
        })
        durationTimer = Timer()
        durationTimer.schedule(object : TimerTask() {
            var temp: Long = 0L
            override fun run() {
                val nowDuration = mBinder.getDuration().toLong()
                if (temp != nowDuration) serviceViewModel.getShowingDuration()
                    .postValue(nowDuration)
                temp = nowDuration
            }
        }, 0, 16)
    }

    override fun onCreate() {
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        serviceViewModel = MusicServiceViewModel.getInstance()
        notificationManager = NotificationUtils.getInstance(application).getNotificationManager()
        initObserver()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onBind(intent: Intent): IBinder {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        durationTimer.cancel()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceViewModel.getPlayingSong().removeObservers(this)
        serviceViewModel.getPlayingDuration().removeObservers(this)
        serviceViewModel.getShowingDuration().removeObservers(this)
        serviceViewModel.getSongList().removeObservers(this)
        super.onDestroy()
    }

    @NonNull
    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }
}