package com.lalilu.lmusic.database

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MediaSource @Inject constructor(
    @ApplicationContext val mContext: Context,
) : CoroutineScope, ContentObserver(Handler(Looper.getMainLooper())) {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val targetUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    init {
        mContext.contentResolver.registerContentObserver(targetUri, true, this)
    }

    val minDurationLimit = 30 * 1000
    val minSizeLimit = 500 * 1024
    val unknownArtist = "<unknown>"

    val _songs: MutableStateFlow<List<MSong>> = MutableStateFlow(ArrayList())
    val _albums: MutableStateFlow<List<MAlbum>> = MutableStateFlow(ArrayList())

    val songs: LiveData<List<MSong>> = _songs.asLiveData()
    val albums: LiveData<List<MAlbum>> = _albums.asLiveData()

    override fun onChange(selfChange: Boolean) {
        getAllSongs()
        getAllAlbums()
    }

    private suspend fun searchForMedia(
        selection: String? = null,
        projection: Array<String>? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Cursor? = withContext(Dispatchers.IO) {
        return@withContext mContext.contentResolver.query(
            targetUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    fun getAllAlbums() = launch {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST
            )
        ) ?: return@launch

        _albums.emit(ArrayList<MAlbum>().apply {
            while (cursor.moveToNext()) {
                val albumId = cursor.getAlbumId()                       // 专辑 id
                val albumTitle = cursor.getAlbumTitle()                 // 专辑标题
                val artistId = cursor.getArtistId()                     // 艺术家 id
                val artistName = cursor.getArtist()                     // 艺术家

                add(
                    MAlbum(
                        albumId = albumId,
                        albumTitle = albumTitle
                    )
                )
            }
        })
    }

    fun getAllSongs() = launch {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE
            ),
            selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                    "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                    "and ${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf("$minSizeLimit", "$minDurationLimit", unknownArtist)
        ) ?: return@launch

        _songs.emit(ArrayList<MSong>().apply {
            while (cursor.moveToNext()) {
                val songId = cursor.getSongId()                         // 音乐 id
                val songTitle = cursor.getSongTitle()                   // 歌曲标题
                val songDuration = cursor.getSongDuration()             // 音乐时长
                val songData = cursor.getSongData()                     // 路径
                val songSize = cursor.getSongSize()                     // 大小
                val songMimeType = cursor.getSongMimeType()             // MIME类型
                val albumId = cursor.getAlbumId()                       // 专辑 id
                val albumTitle = cursor.getAlbumTitle()                 // 专辑标题
                val artistName = cursor.getArtist()                     // 艺术家
                val artistsName = cursor.getArtists().toTypedArray()    // 艺术家
                val songUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId.toString()
                )

                add(
                    MSong(
                        songId = songId,
                        songUri = songUri,
                        albumId = albumId,
                        songData = songData,
                        songTitle = songTitle,
                        albumTitle = albumTitle,
                        showingArtist = artistName,
                        songDuration = songDuration,
                        songMimeType = songMimeType
                    )
                )
            }
        })
    }
}