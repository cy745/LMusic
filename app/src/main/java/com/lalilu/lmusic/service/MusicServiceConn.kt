package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class MusicServiceConn : ServiceConnection {
    var binder: MusicService.MusicBinder? = null
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = service as MusicService.MusicBinder
    }

    override fun onServiceDisconnected(name: ComponentName?) {}
}