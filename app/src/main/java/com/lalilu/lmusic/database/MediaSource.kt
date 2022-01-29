package com.lalilu.lmusic.database

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
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
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    val songs: MutableStateFlow<List<MSong>> = MutableStateFlow(ArrayList())
    var selection: String? = "${MediaStore.Audio.Media.SIZE} >= ? " +
            "and ${MediaStore.Audio.Media.DURATION} >= ? " +
            "and ${MediaStore.Audio.Artists.ARTIST} != ?"
    var projection: Array<String>? = null
    var selectionArgs: Array<String>? =
        arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")
    var sortOrder: String? = null

    private suspend fun searchForMedia(
        selection: String?,
        projection: Array<String>?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? =
        withContext(Dispatchers.IO) {
            return@withContext mContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
        }

    fun getAllSongs() = launch {
        val cursor = searchForMedia(selection, projection, selectionArgs, sortOrder)
        cursor ?: return@launch

        val temp = ArrayList<MSong>()
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

            val song = MSong(
                songUri = songUri,
                songId = songId,
                albumId = albumId,
                albumTitle = albumTitle,
                songTitle = songTitle,
                songDuration = songDuration,
                showingArtist = artistName,
                songMimeType = songMimeType
            )
            temp.add(song)
        }
        songs.emit(temp)
    }
}