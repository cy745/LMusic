package com.lalilu.lmusic.utils.scanner

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lalilu.lmusic.utils.*
import com.lalilu.lmusic.worker.ScannerWorker
import kotlinx.coroutines.Dispatchers
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class MSongScanner @Inject constructor() : BaseMScanner() {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    override var selection: String? = "${MediaStore.Audio.Media.SIZE} >= ? " +
            "and ${MediaStore.Audio.Media.DURATION} >= ? " +
            "and ${MediaStore.Audio.Artists.ARTIST} != ?"
    override var selectionArgs: Array<String>? =
        arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")

    override fun onScanForEach(context: Context, cursor: Cursor) {
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

        val songId = cursor.getSongId()                 // 音乐 id
        val songTitle = cursor.getSongTitle()           // 歌曲标题
        val songDuration = cursor.getSongDuration()     // 音乐时长
        val songData = cursor.getSongData()             // 路径
        val songSize = cursor.getSongSize()             // 大小
        val songMimeType = cursor.getSongMimeType()     // MIME类型
        val albumId = cursor.getAlbumId()               // 专辑 id
        val albumTitle = cursor.getAlbumTitle()         // 专辑标题
        val artistName = cursor.getArtist()             // 艺术家
        val artistsName = cursor.getArtists().toTypedArray()           // 艺术家

        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<ScannerWorker>()
                .addTag("Song_Scan")
                .setInputData(
                    workDataOf(
                        "songId" to songId,
                        "songTitle" to songTitle,
                        "songDuration" to songDuration,
                        "songData" to songData,
                        "songSize" to songSize,
                        "songMimeType" to songMimeType,
                        "albumId" to albumId,
                        "albumTitle" to albumTitle,
                        "artistName" to artistName,
                        "artistsName" to artistsName
                    )
                ).build()
        )
        val taskNum = ++progressCount
        onScanProgress?.invoke(taskNum)
    }
}