package com.lalilu.lmusic.service

import android.net.Uri

interface EnhanceBrowser {
    fun togglePlay(): Boolean
    fun playByUri(uri: Uri): Boolean
    fun playById(mediaId: String): Boolean
    fun playById(mediaId: String, playWhenReady: Boolean): Boolean
    fun addToNext(mediaId: String): Boolean
    fun removeById(mediaId: String): Boolean
    fun revokeRemove(): Boolean
    fun moveByDelta(mediaId: String, delta: Int): Boolean
}