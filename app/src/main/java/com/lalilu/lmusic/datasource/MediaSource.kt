package com.lalilu.lmusic.datasource

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.IntDef
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.domain.entity.MArtist
import com.lalilu.lmusic.domain.entity.MSong

interface MediaSource : Iterable<MediaMetadataCompat> {
    fun getAllSongs(): List<MSong>
    fun getAllAlbums(): List<MAlbum>
    fun getAllArtists(): List<MArtist>

    fun getSongById(id: Long): MSong?
    fun getAlbumById(id: Long): MAlbum?
    fun getArtistById(id: Long): MArtist?

    fun getSongsBySongIds(ids: List<Long>): List<MSong>
    fun getSongsByAlbumId(id: Long): List<MSong>
    fun getSongsByArtistId(id: Long): List<MSong>

    suspend fun load()
    fun whenReady(performAction: (Boolean) -> Unit): Boolean
    fun search(query: String, extras: Bundle): List<MediaMetadataCompat>
}

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

abstract class AbstractMediaSource() : MediaSource {
    private val readyListener = mutableListOf<(Boolean) -> Unit>()

    @ReadyState
    var readyState: Int = STATE_CREATED
        set(value) {
            when (value) {
                STATE_INITIALIZED,
                STATE_ERROR -> synchronized(readyListener) {
                    field = value
                    readyListener
                        .forEach { it.invoke(value != STATE_ERROR) }
                }
                else -> field = value
            }
        }

    override fun whenReady(performAction: (Boolean) -> Unit): Boolean {
        return when (readyState) {
            STATE_CREATED, STATE_INITIALIZING -> {
                readyListener += performAction
                false
            }
            else -> {
                performAction.invoke(readyState != STATE_ERROR)
                true
            }
        }
    }

    override fun search(query: String, extras: Bundle): List<MediaMetadataCompat> {
        val focusSearchResult = when (extras[MediaStore.EXTRA_MEDIA_FOCUS]) {
            MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE -> {
                // For a Genre focused search, only genre is set.
                val genre = extras[MediaStore.EXTRA_MEDIA_GENRE]
                filter { it.genre == genre }
            }
            MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> {
                // For an Artist focused search, only the artist is set.
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                filter { it.artist == artist || it.albumArtist == artist }
            }
            MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> {
                // For an Album focused search, album and artist are set.
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                val album = extras[MediaStore.EXTRA_MEDIA_ALBUM]
                filter { song ->
                    (song.artist == artist || song.albumArtist == artist)
                            && song.album == album
                }
            }
            MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                // For a Song (aka Media) focused search, title, album, and artist are set.
                val title = extras[MediaStore.EXTRA_MEDIA_TITLE]
                val album = extras[MediaStore.EXTRA_MEDIA_ALBUM]
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                filter { song ->
                    (song.artist == artist || song.albumArtist == artist) && song.album == album
                            && song.title == title
                }
            }
            else -> {
                emptyList()
            }
        }
        if (focusSearchResult.isEmpty()) {
            return if (query.isNotBlank()) {
                filter { song ->
                    song.title.containsCaseInsensitive(query)
                            || song.genre.containsCaseInsensitive(query)
                }
            } else {
                // If the user asked to "play music", or something similar, the query will also
                // be blank. Given the small catalog of songs in the sample, just return them
                // all, shuffled, as something to play.
                return shuffled()
            }
        } else {
            return focusSearchResult
        }
    }
}
