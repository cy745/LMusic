package com.lalilu.lmusic.datasource

import android.os.Bundle
import androidx.media3.common.MediaItem

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
    fun search(query: String, extras: Bundle): List<MediaItem>
}
