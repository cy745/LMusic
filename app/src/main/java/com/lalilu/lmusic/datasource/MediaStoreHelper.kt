package com.lalilu.lmusic.datasource

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.extensions.setAlbumId
import com.lalilu.lmusic.datasource.extensions.setArtistId
import com.lalilu.lmusic.datasource.extensions.setDuration
import com.lalilu.lmusic.datasource.extensions.setSongData
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MediaStoreHelper @Inject constructor(
    @ApplicationContext private val mContext: Context
) : BaseReadyHelper(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val targetUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val baseProjection = ArrayList(
        listOf(
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
        )
    ).also {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            it.add(MediaStore.Audio.Media.GENRE)
        }
    }.toTypedArray()

    private val unknownArtist = "<unknown>"
    private val minDurationLimit = 30 * 1000
    private val minSizeLimit = 0

    private val baseSortOrder = "${MediaStore.Audio.Media._ID} DESC"
    private val baseSelections = "${MediaStore.Audio.Media.SIZE} >= ? " +
            "and ${MediaStore.Audio.Media.DURATION} >= ? " +
            "and ${MediaStore.Audio.Artists.ARTIST} != ?"

    private var artistFilter = unknownArtist
    private var minDurationFilter = minDurationLimit

    val mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())

    init {
        SpManager.listen(
            Config.KEY_SETTINGS_MEDIA_UNKNOWN_FILTER,
            SpManager.SpBoolListener(Config.DEFAULT_SETTINGS_MEDIA_UNKNOWN_FILTER) {
                artistFilter = if (it) unknownArtist else ""
                start()
            }, false
        )
        mContext.contentResolver
            .registerContentObserver(targetUri, true, MediaSourceObserver())
    }

    override fun initialize() {
        mediaItems.tryEmit(
            searchForMedia(
                projection = baseProjection,
                selection = baseSelections,
                sortOrder = baseSortOrder,
                selectionArgs = arrayOf(
                    minSizeLimit.toString(),
                    minDurationFilter.toString(),
                    artistFilter
                )
            ).getMediaItems()
        )
    }

    inner class MediaSourceObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            launch(Dispatchers.IO) { start() }
        }
    }

    private fun Cursor?.getMediaItems(): ArrayList<MediaItem> {
        this ?: return ArrayList()
        return ArrayList<MediaItem>().apply {
            while (this@getMediaItems.moveToNext()) {
                add(
                    MediaItem.Builder()
                        .from(this@getMediaItems)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .from(this@getMediaItems)
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
        setTitle(cursor.getSongTitle())
        setMediaUri(cursor.getMediaUri())
        setAlbumArtist(cursor.getArtist())
        setArtworkUri(cursor.getAlbumArt())
        setAlbumTitle(cursor.getAlbumTitle())
        setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
        setIsPlayable(true)
        setExtras(
            Bundle()
                .setAlbumId(cursor.getAlbumId())
                .setArtistId(cursor.getArtistId())
                .setSongData(cursor.getSongData())
                .setDuration(cursor.getSongDuration())
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val genre = cursor.getSongGenre() ?: "Empty"
            setGenre(genre.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
        }
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