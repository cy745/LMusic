package com.lalilu.lmusic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 负责清除不存在的歌曲和空白专辑
 */
@HiltWorker
class DeleteNonExistDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val ids = inputData.getLongArray("ids")?.toList()
                ?: throw NullPointerException("获取扫描结果失败")

            repository.songDao.getAllId().forEach {
                if (!ids.contains(it)) {
                    repository.songDao.deleteById(it)
                    repository.songDetailDao.deleteById(it)
                    repository.relationDao.deleteBySongId(it)
                }
            }

            repository.albumDao.getAllAlbumWithSong().forEach { albumWithSong ->
                if (albumWithSong.songs.isEmpty()) {
                    repository.albumDao.deleteById(albumWithSong.album.albumId)
                }
            }
            Result.success()
        } catch (e: Exception) {
            println(e.message)
            Result.failure()
        }
    }

}