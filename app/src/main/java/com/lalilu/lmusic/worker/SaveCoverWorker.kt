package com.lalilu.lmusic.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.utils.BitmapUtils
import com.lalilu.lmusic.utils.ThreadPoolUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 负责提取并保存音频文件内嵌的封面
 */
@HiltWorker
class SaveCoverWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            val songId = inputData.getLong("songId", -1L)
            val songUri = Uri.parse(inputData.getString("songUri"))

            val songCoverUri = BitmapUtils.saveThumbnailToSandBox(
                applicationContext, songUri
            )
            repository.updateSongCoverUri(songId, songCoverUri)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}