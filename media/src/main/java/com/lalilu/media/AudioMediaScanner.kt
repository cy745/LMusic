package com.lalilu.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.common.bitmap.BitmapUtils
import com.lalilu.media.database.LMusicDatabase
import com.lalilu.media.entity.LMusicAlbum
import com.lalilu.media.entity.LMusicMedia
import com.lalilu.media.entity.LMusicPlayList
import java.io.File
import java.util.logging.Logger


class AudioMediaScanner constructor(private val mContext: Context, database: LMusicDatabase) {
    private val logger = Logger.getLogger(this.javaClass.name)
    private val mediaItemDao = database.mediaItemDao()
    private val albumDao = database.albumDao()
    private val playListDao = database.playlistDao()

    @Volatile
    private var standardDirectory: File =
        mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    private var running: Boolean = false
    fun updateSongDataBase(callback: (String) -> Unit?) {
        if (running) return else running = true
        playListDao.deleteAll()
        mediaItemDao.deleteAll()
        albumDao.deleteAll()

        val selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                "and ${MediaStore.Audio.Media.DURATION} >= ?" +
                "and ${MediaStore.Audio.Artists.ARTIST} != ?"

        val selectionArgs =
            arrayOf(minSize.toString(), minDuration.toString(), "<unknown>")

        val cursor = mContext.contentResolver.query(
            externalUri, null, selection, selectionArgs, null
        )

        val playList = LMusicPlayList().also {
            it.playListId = 0
            it.playListTitle = "全部"
        }
        while (cursor!!.moveToNext()) {
            val song = getSong(cursor)
            val album = getAlbum(song)

            song.let {
                BitmapUtils.saveThumbnailToSandBox(
                    mContext, standardDirectory,
                    it.mediaId, it.mediaUri
                )
                it.mediaArtUri = BitmapUtils.loadThumbnail(standardDirectory, it.mediaId)
                mediaItemDao.insert(it)
                if (playList.playListArtUri == Uri.EMPTY) {
                    playList.playListArtUri = it.mediaArtUri
                }
                playList.mediaIdList.add("${it.mediaTitle} - ${it.mediaArtist}")
            }
            album?.let { albumDao.insert(album) }
        }
        playListDao.insert(playList)
        cursor.close()
        callback("扫描完成: EXTERNAL_CONTENT_URI: ${MediaStore.Files.getContentUri("external")}")
        running = false
    }

    fun getMediaMetaData(): List<MediaMetadataCompat> {
        return mediaItemDao.getAll().map { it.toMediaMetaData() }
    }

    fun getPlayList(): List<LMusicPlayList> {
        return playListDao.getAll()
    }

    companion object {
        private val externalUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")
        private const val minSize = 500 * 1024
        private const val minDuration = 30 * 1000

        private fun getAlbum(lMusicMedia: LMusicMedia?): LMusicAlbum? {
            if (lMusicMedia == null) return null

            return LMusicAlbum().also {
                it.albumId = lMusicMedia.albumId
                it.albumUri = lMusicMedia.albumUri
                it.albumTitle = lMusicMedia.albumTitle
                it.albumArtist = lMusicMedia.albumArtist
            }
        }

        private fun getSong(cursor: Cursor): LMusicMedia {
            return LMusicMedia().also {
                it.mediaId = getLong(cursor, MediaStore.Audio.Media._ID)
                it.mediaSize = getLong(cursor, MediaStore.Audio.Media.SIZE)
                it.mediaArtist = getString(cursor, MediaStore.Audio.Artists.ARTIST)
                it.mediaDuration = getLong(cursor, MediaStore.Audio.Media.DURATION)
                it.mediaTitle = getString(cursor, MediaStore.Audio.Media.TITLE)
                it.mediaMimeType = getString(cursor, MediaStore.Audio.Media.MIME_TYPE)
                it.albumId = getLong(cursor, MediaStore.Audio.Albums.ALBUM_ID)
                it.albumTitle = getString(cursor, MediaStore.Audio.Albums.ALBUM)
                it.albumArtist = getString(cursor, MediaStore.Audio.Albums.ARTIST)
                it.albumUri = ContentUris.withAppendedId(externalAlbumUri, it.albumId)
                it.mediaUri = Uri.withAppendedPath(externalUri, it.mediaId.toString())
            }
        }

        private fun getString(cursor: Cursor, string: String): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(string))
        }

        private fun getLong(cursor: Cursor, string: String): Long {
            return cursor.getLong(cursor.getColumnIndexOrThrow(string))
        }
    }
}