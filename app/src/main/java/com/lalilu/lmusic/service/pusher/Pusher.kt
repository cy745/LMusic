package com.lalilu.lmusic.service.pusher


interface Pusher {
    fun update()
    fun cancel()
    fun destroy()
}