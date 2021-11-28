package com.lalilu.lmusic.utils.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.*
import com.lalilu.lmusic.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger


object MSongScanner : BaseMScanner() {
    private var database: LMusicDataBase = LMusicDataBase.getInstance(null)
    private val mExecutor = ThreadPoolUtils.newCachedThreadPool()
    override var selection: String? = "${MediaStore.Audio.Media.SIZE} >= ? " +
            "and ${MediaStore.Audio.Media.DURATION} >= ?" +
            "and ${MediaStore.Audio.Artists.ARTIST} != ?"
    override var selectionArgs: Array<String>? =
        arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")

    init {
        setScanForEach { context, cursor -> scanForEach(context, cursor) }
    }

    private fun scanForEach(context: Context, cursor: Cursor) {
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

        val songId = cursor.getSongId()                 // 音乐 id
        val songTitle = cursor.getSongTitle()           // 歌曲标题
        val songDuration = cursor.getSongDuration()     // 音乐时长
        val songData = cursor.getSongData()             // 路径
        val songSize = cursor.getSongSize()             // 大小
        val songMimeType = cursor.getSongMimeType()     // MIME类型
        val albumId = cursor.getAlbumId()               // 专辑 id
        val albumTitle = cursor.getAlbumTitle()         // 专辑标题
        val artistName = cursor.getArtist()                 // 艺术家
        val artistsName = cursor.getArtists()               // 艺术家

        val taskNumber = ++progressCount
        taskList.add(GlobalScope.launch(mExecutor.asCoroutineDispatcher()) {
            val songUri = Uri.withAppendedPath(EXTERNAL_CONTENT_URI, songId.toString())

            val album = MAlbum(albumId, albumTitle)
            database.albumDao().save(album)

            val songCoverUri = BitmapUtils.saveThumbnailToSandBox(context, songId, songUri)

            try {
                val audioTag = AudioFileIO.read(File(songData)).tag
                val lyric = audioTag.getFields(FieldKey.LYRICS)
                    .run { if (isNotEmpty()) get(0).toString() else "" }
                val detail = MSongDetail(songId, lyric, songSize, songData)

                database.songDetailDao().save(detail)
            } catch (_: Exception) {
            }
            val song = MSong(
                songUri = songUri,
                songId = songId,
                albumId = albumId,
                albumTitle = albumTitle,
                songTitle = songTitle,
                songDuration = songDuration,
                showingArtist = artistName,
                songCoverUri = songCoverUri,
                songMimeType = songMimeType
            )

            val artists: List<MArtist> = artistsName.map { MArtist(it) }
            database.relationDao().saveSongXArtist(song, artists)
            database.relationDao().savePlaylistXSong(MPlaylist(0), listOf(song))

            onScanProgress?.invoke(taskNumber)
        })
    }

    fun check() {
        checkDatabase()

        if (database == null) database = LMusicDataBase.getInstance(null)

        GlobalScope.launch(Dispatchers.IO) {
            database!!.albumDao().getAllFullAlbum().forEach {
                println("\n")
                println("==================================")
                println("【${it.album.albumTitle}】 ${it.songs.size}")
                it.songs.forEach { song ->
                    println("-- ${song.songTitle}")
                }
                println("==================================")
            }

            database!!.artistDao().delete(MArtist("DiverDiva"))
        }
    }

    private fun checkDatabase() {
        database = database ?: LMusicDataBase.getInstance(null)
    }
}