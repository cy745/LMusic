package com.lalilu.lmusic.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lalilu.lmusic.utils.*
import com.lalilu.lmusic.worker.*
import kotlinx.coroutines.Dispatchers
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Singleton
class MSongScanner @Inject constructor() : BaseMScanner() {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    override var selection: String? = "${MediaStore.Audio.Media.SIZE} >= ? " +
            "and ${MediaStore.Audio.Media.DURATION} >= ? " +
            "and ${MediaStore.Audio.Artists.ARTIST} != ?"
    override var selectionArgs: Array<String>? =
        arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")

    override fun onScanForEach(context: Context, cursor: Cursor) {
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

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

        val saveSongRequest = OneTimeWorkRequestBuilder<ScannerWorker>()
            .addTag("Save_Song")
            .setInputData(
                workDataOf(
                    "songId" to songId,
                    "songTitle" to songTitle,
                    "songDuration" to songDuration,
                    "songMimeType" to songMimeType,
                    "songUri" to songUri.toString(),
                    "albumId" to albumId,
                    "albumTitle" to albumTitle,
                    "artistName" to artistName,
                )
            ).build()

        val saveAlbumRequest = OneTimeWorkRequestBuilder<SaveAlbumWorker>()
            .addTag("Save_Album")
            .setInputData(
                workDataOf(
                    "albumId" to albumId,
                    "albumTitle" to albumTitle
                )
            ).build()

        val saveArtistRequest = OneTimeWorkRequestBuilder<SaveArtistWorker>()
            .addTag("Save_Artist")
            .setInputData(
                workDataOf(
                    "songId" to songId,
                    "artistsName" to artistsName
                )
            ).build()

        val saveCoverRequest = OneTimeWorkRequestBuilder<SaveCoverWorker>()
            .addTag("Save_Cover")
            .setInputData(
                workDataOf(
                    "songId" to songId,
                    "songUri" to songUri.toString()
                )
            ).build()

        val saveLyricRequest = OneTimeWorkRequestBuilder<SaveLyricWorker>()
            .addTag("Save_Lyric")
            .setInputData(
                workDataOf(
                    "songId" to songId,
                    "songSize" to songSize,
                    "songData" to songData
                )
            ).build()

        WorkManager.getInstance(context).beginUniqueWork(
            "Save_Song_$songId",
            ExistingWorkPolicy.REPLACE,
            saveSongRequest
        ).then(
            listOf(
                saveAlbumRequest,
                saveArtistRequest,
                saveCoverRequest,
                saveLyricRequest
            )
        ).enqueue()

        val taskNum = ++progressCount
        onScanProgress?.invoke(taskNum)
    }
}