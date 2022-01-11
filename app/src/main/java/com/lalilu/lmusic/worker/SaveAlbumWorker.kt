package com.lalilu.lmusic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.utils.ThreadPoolUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 负责保存专辑进数据库
 */
@HiltWorker
class SaveAlbumWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            val albumId = inputData.getLong("albumId", -1L)
            val albumTitle = inputData.getString("albumTitle")

            val album = MAlbum(albumId, albumTitle!!)
            repository.saveAlbum(album)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}