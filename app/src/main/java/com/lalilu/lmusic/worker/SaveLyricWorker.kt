package com.lalilu.lmusic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.utils.ThreadPoolUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 负责提取并保存歌词
 */
@HiltWorker
class SaveLyricWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            Logger.getLogger("org.jaudiotagger").level = Level.OFF
            val songId = inputData.getLong("songId", -1L)
            val songSize = inputData.getLong("songSize", -1L)
            val songData = inputData.getString("songData")

            if (songData == null) Result.failure()

            val audioTag = AudioFileIO.readMagic(File(songData!!)).tag
            val lyric = audioTag.getFields(FieldKey.LYRICS).run {
                if (isNotEmpty()) get(0).toString() else ""
            }
            repository.updateSongLyric(songId, lyric, songSize, songData)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}