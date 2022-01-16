package com.lalilu.lmusic.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.MSongDetail
import com.lalilu.lmusic.utils.ThreadPoolUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class ScannerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            val songId = inputData.getLong("songId", -1L)
            val songTitle = inputData.getString("songTitle")
            val songDuration = inputData.getLong("songDuration", -1L)
            val songMimeType = inputData.getString("songMimeType")
            val songUri = Uri.parse(inputData.getString("songUri"))
            val albumId = inputData.getLong("albumId", -1L)
            val albumTitle = inputData.getString("albumTitle")
            val artistName = inputData.getString("artistName")

            val song = MSong(
                songUri = songUri,
                songId = songId,
                albumId = albumId,
                albumTitle = albumTitle!!,
                songTitle = songTitle!!,
                songDuration = songDuration,
                showingArtist = artistName!!,
                songMimeType = songMimeType!!
            )

            repository.savePlaylistXSong(MPlaylist(0), song)
            repository.saveSongDetail(MSongDetail(songId))
            Result.success(workDataOf("msg" to songTitle))
        } catch (e: Exception) {
            Result.failure(workDataOf("msg" to e.message))
        }
    }
}