package com.lalilu.lmedia.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.lalilu.common.toUpdatableFlow
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.parseId3GenreName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * 参照Auxio实现的Backend部分代码精简而成
 */
open class MediaStoreScanner(
    private val context: Context
) : MediaSource<LSong> {
    companion object {

        @Suppress("inlinedApi")
        private const val VOLUME_EXTERNAL = MediaStore.VOLUME_EXTERNAL

        @Suppress("inlinedApi")
        private const val AUDIO_COLUMN_ALBUM_ARTIST = MediaStore.Audio.AudioColumns.ALBUM_ARTIST
        private const val BASE_SELECTOR =
            "${MediaStore.Audio.Media.SIZE} >= 10 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
        private const val BASE_SORT_ORDER = "${MediaStore.Audio.Media._ID} DESC"
    }

    /**
     * 基础字段
     */
    private var idIndex = -1
    private var titleIndex = -1
    private var displayNameIndex = -1
    private var mimeTypeIndex = -1
    private var sizeIndex = -1
    private var dateAddedIndex = -1
    private var dateModifiedIndex = -1
    private var durationIndex = -1
    private var yearIndex = -1
    private var albumIndex = -1
    private var albumIdIndex = -1
    private var artistIndex = -1
    private var artistIdIndex = -1
    private var albumArtistIndex = -1

    private var genreIdIndex = -1
    private var genreNameIndex = -1

    open val projection: Array<String> = arrayOf(
        // These columns are guaranteed to work on all versions of android
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.MIME_TYPE,
        MediaStore.Audio.AudioColumns.SIZE,
        MediaStore.Audio.AudioColumns.DATE_ADDED,
        MediaStore.Audio.AudioColumns.DATE_MODIFIED,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.YEAR,
        MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.AudioColumns.ALBUM_ID,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.ARTIST_ID,
        AUDIO_COLUMN_ALBUM_ARTIST
    )

    private fun query(context: Context): Cursor {
        return requireNotNull(
            context.applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, BASE_SELECTOR, null, BASE_SORT_ORDER
            )
        )
    }

    private fun queryGenre(context: Context): Cursor {
        return requireNotNull(
            context.applicationContext.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
                null, null, null
            )
        )
    }

    private val resultFlow = flow { emit(retrieve()) }
        .toUpdatableFlow(debouncingInterval = 2000L)

    override fun updateAsync() = resultFlow.requireUpdate()
    override fun requireFlow(): Flow<List<LSong>> = resultFlow

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            this@MediaStoreScanner.updateAsync()
        }
    }

    init {
        context.applicationContext.contentResolver
            .unregisterContentObserver(observer)

        context.applicationContext.contentResolver
            .registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
    }

    private suspend fun retrieve(): List<LSong> = withContext(Dispatchers.Default) {
        val genresFetchJob = async {
            val tempGenres = queryGenres()
            buildGenresMap(tempGenres)
        }

        val songsFetchJob = async {
            val cursor = query(context)
            buildList { while (cursor.moveToNext()) add(buildAudio(cursor)) }
        }

        val genresMap = genresFetchJob.await()
        songsFetchJob.await().toList().mapNotNull {
            it.toSong(genre = genresMap[it.id.toString()] ?: "")
        }
    }

    /**
     * 初始化查询Genre数据
     */
    @SuppressLint("Range")
    private fun queryGenres(): List<Pair<Long, String>> {
        val cursor = queryGenre(context)

        val genres = mutableListOf<Pair<Long, String>>()
        while (cursor.moveToNext()) {
            if (genreIdIndex == -1) {
                genreIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                genreNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
            }

            val name = (cursor.getStringOrNull(genreNameIndex) ?: continue).parseId3GenreName()
            val id = cursor.getLong(genreIdIndex)

            genres.add(id to name)
        }
        return genres
    }


    /**
     * 将Genres与LSong的关系提取成Map
     */
    private suspend fun buildGenresMap(genres: List<Pair<Long, String>>) =
        withContext(Dispatchers.Default) {
            genres.map { genre ->
                async {
                    val list = context.applicationContext.contentResolver.query(
                        MediaStore.Audio.Genres.Members.getContentUri(VOLUME_EXTERNAL, genre.first),
                        arrayOf(MediaStore.Audio.Genres.Members._ID), null, null, null
                    )?.use {
                        val songIdIndex =
                            it.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members._ID)
                        buildList {
                            while (it.moveToNext()) add(it.getStringOrNull(songIdIndex))
                        }.filterNotNull()
                    } ?: emptyList()

                    list.map { it to genre.second }
                }
            }.awaitAll()
                .flatten()
                .toMap()
        }

    @SuppressLint("Range")
    protected open fun buildAudio(cursor: Cursor): Audio {
        if (idIndex == -1) {
            idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            displayNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.MIME_TYPE)
            sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE)
            dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED)
            durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR)
            albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
            albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            artistIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID)
            albumArtistIndex = cursor.getColumnIndexOrThrow(AUDIO_COLUMN_ALBUM_ARTIST)
        }

        val audio = Audio(
            id = cursor.getLong(idIndex),
            title = cursor.getString(titleIndex),
            extensionMimeType = cursor.getString(mimeTypeIndex),
            size = cursor.getLong(sizeIndex),
            duration = cursor.getLong(durationIndex),
            // A non-existent album name should theoretically be the name of the folder it contained
            // in, but in practice it is more often "0" (as in /storage/emulated/0), even when it the
            // file is not actually in the root internal storage directory. We can't do anything to
            // fix this, really.
            album = cursor.getString(albumIndex),
            albumId = cursor.getLong(albumIdIndex),
            // Android does not make a non-existent artist tag null, it instead fills it in
            // as <unknown>, which makes absolutely no sense given how other fields default
            // to null if they are not present. If this field is <unknown>, null it so that
            // it's easier to handle later.
            artist = cursor.getString(artistIndex),
            artistId = cursor.getString(artistIdIndex),
            date = cursor.getIntOrNull(yearIndex)?.toString()
        )

        // The album artist field is nullable and never has placeholder values.
        audio.albumArtist = cursor.getStringOrNull(albumArtistIndex)

        // Try to use the DISPLAY_NAME field to obtain a (probably sane) file name
        // from the android system.
        audio.fileName = cursor.getStringOrNull(displayNameIndex)
        audio.dateAdded = cursor.getLongOrNull(dateAddedIndex)
        audio.dateModified = cursor.getLongOrNull(dateAddedIndex)
//        audio.date = cursor.getIntOrNull(yearIndex)?.let(Date::from)
        return audio
    }
}
