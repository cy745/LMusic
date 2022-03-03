package com.lalilu.lmusic.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.FOLDER_TYPE_PLAYLISTS
import com.lalilu.lmusic.datasource.extensions.*
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@SuppressLint("UnsafeOptInUsageError")
class BaseMediaSource @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val dataBase: LMusicDataBase
) : CoroutineScope, AbstractMediaSource() {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val targetUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val minDurationLimit = 30 * 1000
    private val minSizeLimit = 500 * 1024
    private val unknownArtist = "<unknown>"

    init {
        mContext.contentResolver
            .registerContentObserver(targetUri, true, MediaSourceObserver())
    }

    inner class MediaSourceObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {

        }
    }

    override fun getAlbumIdFromMediaItem(mediaItem: MediaItem): String {
        return mediaItem.mediaMetadata.getAlbumId().toString()
    }

    override fun getArtistIdFromMediaItem(mediaItem: MediaItem): String {
        return mediaItem.mediaMetadata.getArtistId().toString()
    }

    override fun songItemToAlbumItem(mediaItem: MediaItem): MediaItem {
        return MediaItem.Builder()
            .setMediaId(getAlbumIdFromMediaItem(mediaItem))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(mediaItem.mediaMetadata.albumTitle)
                    .setAlbumTitle(mediaItem.mediaMetadata.albumTitle)
                    .setArtist(mediaItem.mediaMetadata.artist)
                    .setAlbumArtist(mediaItem.mediaMetadata.albumArtist)
                    .setArtworkUri(mediaItem.mediaMetadata.artworkUri)
                    .setIsPlayable(false)
                    .setFolderType(FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }

    override fun songItemToArtistItem(mediaItem: MediaItem): MediaItem {
        return MediaItem.Builder()
            .setMediaId(getArtistIdFromMediaItem(mediaItem))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(mediaItem.mediaMetadata.artist)
                    .setAlbumTitle(mediaItem.mediaMetadata.artist)
                    .setArtist(mediaItem.mediaMetadata.artist)
                    .setIsPlayable(false)
                    .setFolderType(FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }

    override fun songItemToGenreItem(mediaItem: MediaItem): MediaItem {
        return MediaItem.Builder()
            .setMediaId(mediaItem.mediaMetadata.genre.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(mediaItem.mediaMetadata.genre.toString())
                    .setAlbumTitle(mediaItem.mediaMetadata.genre.toString())
                    .setIsPlayable(false)
                    .setFolderType(FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }

    override suspend fun load() {
        try {
            initialize(loadMediaItems())
        } catch (e: Exception) {
            readyState = STATE_ERROR
        }
    }

    private suspend fun loadMediaItems(): MutableList<MediaItem> =
        withContext(Dispatchers.IO) {
            val cursor = searchForMedia(
                projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.MIME_TYPE
                ),
                selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                        "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                        "and ${MediaStore.Audio.Artists.ARTIST} != ?",
                selectionArgs = arrayOf("$minSizeLimit", "$minDurationLimit", unknownArtist)
            ) ?: return@withContext ArrayList()

            return@withContext ArrayList<MediaItem>().apply {
                while (cursor.moveToNext()) {
                    add(
                        MediaItem.Builder()
                            .from(cursor)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .from(cursor)
                                    .build()
                            ).build()
                    )
                }
            }
        }

    fun MediaItem.Builder.from(cursor: Cursor): MediaItem.Builder {
        setMediaId(cursor.getSongId().toString())
        setMimeType(cursor.getSongMimeType())
        setUri(cursor.getMediaUri())
        return this
    }

    fun MediaMetadata.Builder.from(cursor: Cursor): MediaMetadata.Builder {
        setArtist(cursor.getArtist())
        setAlbumTitle(cursor.getAlbumTitle())
        setTitle(cursor.getSongTitle())
        setMediaUri(cursor.getMediaUri())
        setAlbumArtist(cursor.getArtist())
        setArtworkUri(cursor.getAlbumArt())
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setExtras(
            Bundle()
                .setAlbumId(cursor.getAlbumId())
                .setArtistId(cursor.getArtistId())
                .setDuration(cursor.getSongDuration())
                .setSongData(cursor.getSongData())
        )
        return this
    }

    private fun searchForMedia(
        selection: String? = null,
        projection: Array<String>? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Cursor? {
        return mContext.contentResolver.query(
            targetUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }
}