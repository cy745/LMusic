package com.lalilu.lmusic.datasource

import android.os.Bundle
import androidx.annotation.IntDef
import androidx.media3.common.MediaItem

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

const val ROOT_ID = "[rootID]"
const val ALBUM_ID = "[albumID]"
const val GENRE_ID = "[genreID]"
const val ARTIST_ID = "[artistID]"
const val ALL_ID = "[allID]"

const val ALBUM_PREFIX = "[album]"
const val GENRE_PREFIX = "[genre]"
const val ARTIST_PREFIX = "[artist]"
const val ITEM_PREFIX = "[item]"
const val ALL_PREFIX = "[all]"


interface MediaSource {
    suspend fun load()
    fun whenReady(performAction: suspend (Boolean) -> Unit): Boolean
    fun search(query: String, extras: Bundle): List<MediaItem>
    suspend fun onUpdate()
}
