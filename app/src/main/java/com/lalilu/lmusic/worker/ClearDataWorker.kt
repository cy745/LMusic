package com.lalilu.lmusic.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.utils.TempUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 负责清除不存在的歌曲和空白专辑
 */
@HiltWorker
class ClearDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository,
    private val tempUtils: TempUtils
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            var ids = tempUtils.value["ids"] as List<*>?
                ?: throw NullPointerException("数据获取失败")
            ids = ids.mapNotNull { it as Long }

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
            tempUtils.value.clear()
            Result.success()
        } catch (e: Exception) {
            println(e.message)
            Result.failure()
        }
    }

}