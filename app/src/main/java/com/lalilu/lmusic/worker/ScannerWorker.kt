package com.lalilu.lmusic.worker

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lalilu.lmusic.database.repository.ScannerRepository
import com.lalilu.lmusic.domain.entity.*
import com.lalilu.lmusic.utils.BitmapUtils
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

@HiltWorker
class ScannerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    val repository: ScannerRepository
) : CoroutineWorker(context, workerParams) {
    private val mExecutor = ThreadPoolUtils.CachedThreadPool

    override suspend fun doWork(): Result = withContext(mExecutor.asCoroutineDispatcher()) {
        try {
            Logger.getLogger("org.jaudiotagger").level = Level.OFF

            val songId = inputData.getLong("songId", -1L)
            val songTitle = inputData.getString("songTitle")
            val songDuration = inputData.getLong("songDuration", -1L)
            val songData = inputData.getString("songData")
            val songSize = inputData.getLong("songSize", -1L)
            val songMimeType = inputData.getString("songMimeType")
            val albumId = inputData.getLong("albumId", -1L)
            val albumTitle = inputData.getString("albumTitle")
            val artistName = inputData.getString("artistName")
            val artistsName = inputData.getStringArray("artistsName")?.toList()

            val songUri =
                Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId.toString())

            val album = MAlbum(albumId, albumTitle!!)
            repository.saveAlbum(album)

            val songCoverUri = BitmapUtils.saveThumbnailToSandBox(
                applicationContext, songId, songUri
            )

            try {
                val audioTag = AudioFileIO.read(File(songData)).tag
                val lyric = audioTag.getFields(FieldKey.LYRICS)
                    .run { if (isNotEmpty()) get(0).toString() else "" }
                val detail = MSongDetail(songId, lyric, songSize, songData!!)

                repository.saveSongDetail(detail)
            } catch (e: Exception) {
                println(e.message)
            }

            val song = MSong(
                songUri = songUri,
                songId = songId,
                albumId = albumId,
                albumTitle = albumTitle,
                songTitle = songTitle!!,
                songDuration = songDuration,
                showingArtist = artistName!!,
                songCoverUri = songCoverUri,
                songMimeType = songMimeType!!
            )

            val artists: List<MArtist> = artistsName!!.map { MArtist(it) }
            repository.saveSongXArtist(song, artists)
            repository.savePlaylistXSong(MPlaylist(0), song)

            println("【Done】: $songTitle")
            Result.success()
        } catch (e: Exception) {
            Result.failure(workDataOf("msg" to e.message))
        }
    }
}