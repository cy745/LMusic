package com.lalilu.lmusic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.domain.entity.MArtist
import com.lalilu.lmusic.utils.ThreadPoolUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 负责保存歌手进数据库
 */
@HiltWorker
class SaveArtistWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            val songId = inputData.getLong("songId", -1L)
            val artistsName = inputData.getStringArray("artistsName")?.toList()

            val artists: List<MArtist> = artistsName!!.map { MArtist(it) }
            repository.saveSongXArtist(songId, artists)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}