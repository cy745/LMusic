package com.lalilu.lmedia.indexer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LFolder
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.link
import com.lalilu.lmedia.entity.merge
import com.lalilu.lmedia.entity.separate
import com.lalilu.lmedia.extension.PermissionUtils
import com.lalilu.lmedia.scanner.Api21MediaStoreScanner
import com.lalilu.lmedia.scanner.Api29MediaStoreScanner
import com.lalilu.lmedia.scanner.Api30MediaStoreScanner
import com.lalilu.lmedia.scanner.MediaStoreScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@SuppressLint("ObsoleteSdkInt")
@OptIn(UnstableApi::class)
class Indexer(
    private val library: BaseLibrary
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var scanner: MediaStoreScanner
    private var loopJob: Job? = null

    fun init(context: Context) {
        // 校验当前是否已经有循环任务正在进行中
        if (loopJob?.isActive == true) {
            LogUtils.i("[LMedia]: Already has a loop job running.")
            return
        }

        // 若检测权限未通过，则打印日志
        if (!PermissionUtils.check(context)) {
            LogUtils.i("[LMedia]: ${PermissionUtils.permission} notGrant")
            library.updateState(LibraryState.NotGranted(Throwable("[LMedia]: ${PermissionUtils.permission} notGrant")))
            return
        }

        library.updateState(LibraryState.Loading)
        scanner = when {
            Build.VERSION.SDK_INT >= 30 -> Api30MediaStoreScanner(context)
            Build.VERSION.SDK_INT >= 29 -> Api29MediaStoreScanner(context)
            Build.VERSION.SDK_INT >= 21 -> Api21MediaStoreScanner(context)
            else -> MediaStoreScanner(context)
        }

        val startAt = System.currentTimeMillis()
        LogUtils.i("start index: $startAt")

        loopJob = scanner.requireFlow()
            .onEach {
                LogUtils.i("end index: cost: ${System.currentTimeMillis() - startAt}")
                library.updateState(LibraryState.Loading)
                runCatching { handleIndex(it) }
            }
            .launchIn(this)
    }

    private suspend fun handleIndex(songs: List<LSong>) = withContext(Dispatchers.IO) {
        val buildSongsJob = async {
            songs.associateBy { it.id }
                .also { library.set(it) }
        }

        val buildAlbumJob = async {
            retrieveAlbums(songs)
                .merge()    // 将具有相同名称的LAlbum进行合并
                .link()
                .associateBy { it.id }
                .also { library.set(it) }
        }

        val buildArtistJob = async {
            retrieveArtists(songs)
                .separate()     // separate() 将LArtist拆分成多个LArtist
                .merge()        // merge() 将具有相同name的LArtist合并为同一个
                .link()
                // 不根据 ID 构建索引，artistId 现在没多大用处，使用artist的name作为索引可以方便获取歌曲
                .associateBy { it.name }
                .also { library.set(it) }
        }

        val buildGenreJob = async {
            retrieveGenres(songs)
                .link()
                .associateBy { it.id }
                .also { library.set(it) }
        }

        val buildFolderJob = async {
            retrieveFolders(songs)
                .link()
                .associateBy { it.id }
                .also { library.set(it) }
        }

        listOf(
            buildSongsJob,
            buildAlbumJob,
            buildArtistJob,
            buildGenreJob,
            buildFolderJob
        ).joinAll()

        library.updateState(LibraryState.Ready())
    }

    private fun retrieveGenres(songs: Collection<LSong>): List<LGenre> =
        songs.groupBy { song -> song.metadata.genre.takeIf { it.isNotBlank() } ?: "Uncategorized" }
            .map {
                LGenre(
                    id = it.key,
                    name = it.key,
                    songs = it.value
                )
            }

    private fun retrieveAlbums(songs: Collection<LSong>): List<LAlbum> =
        songs.groupBy { "${it.metadata.album}-${it.metadata.albumArtist}" }
            .map {
                LAlbum(
                    id = it.key,
                    name = it.value[0].metadata.album,
                    artistName = it.value[0].metadata.albumArtist,
                    songs = it.value
                )
            }

    private fun retrieveArtists(songs: Collection<LSong>): List<LArtist> =
        songs.groupBy { it.metadata.artist }
            .map {
                LArtist(
                    name = it.key,
                    songs = it.value
                )
            }

    private fun retrieveFolders(songs: Collection<LSong>): List<LFolder> =
        songs.groupBy { it.fileInfo.directoryPath }
            .map {
                LFolder(
                    path = it.key,
                    songs = it.value
                )
            }
}