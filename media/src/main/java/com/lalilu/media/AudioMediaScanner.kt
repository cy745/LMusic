package com.lalilu.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.lalilu.media.database.LMusicDatabase
import com.lalilu.media.entity.LMusicAlbum
import com.lalilu.media.entity.LMusicMediaItem
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Logger


class AudioMediaScanner constructor(private val mContext: Context, database: LMusicDatabase) {
    private val logger = Logger.getLogger(this.javaClass.name)
    private val mediaItemDao = database.mediaItemDao()
    private val albumDao = database.albumDao()

    @Volatile
    private var standardDirectory: File =
        mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!


    private var running: Boolean = false
    fun updateSongDataBase(callback: () -> Unit?) {
        if (running) return
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

        if (cursor!!.moveToFirst()) {
            do {
                val song = getSong(cursor)
                val album = getAlbum(song)

                if (song != null) {
                    saveThumbnailToSandBox(mContext, song)
                    song.songArtUri = loadThumbnail(song)
                    mediaItemDao.insert(song)
                }
                if (album != null) {
                    albumDao.insert(album)
                }
            } while (cursor.moveToNext())
        } else {
            logger.info("[FOUND NO SONG]")
        }
        cursor.close()
        callback()
        running = false
    }

    private fun saveThumbnailToSandBox(context: Context, lMusicMediaItem: LMusicMediaItem) {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(context, lMusicMediaItem.songUri)

        val embeddedPic = metadataRetriever.embeddedPicture ?: return
        val outputStream = FileOutputStream("$standardDirectory/${lMusicMediaItem.songId}")
        outputStream.write(embeddedPic)
        outputStream.flush()
        outputStream.close()
    }

    private fun loadThumbnail(lMusicMediaItem: LMusicMediaItem): Uri {
        val file = File("$standardDirectory/${lMusicMediaItem.songId}")
        return Uri.fromFile(file)
    }

    companion object {
        private val externalUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")
        private const val minSize = 500 * 1024
        private const val minDuration = 30 * 1000

        private fun getAlbum(lMusicMediaItem: LMusicMediaItem?): LMusicAlbum? {
            if (lMusicMediaItem == null) return null

            return LMusicAlbum().also {
                it.albumId = lMusicMediaItem.albumId
                it.albumUri = lMusicMediaItem.albumUri
                it.albumTitle = lMusicMediaItem.albumTitle
                it.albumArtist = lMusicMediaItem.albumArtist
            }
        }

        private fun getSong(cursor: Cursor): LMusicMediaItem? {
            return LMusicMediaItem().also {
                it.songId = getLong(cursor, MediaStore.Audio.Media._ID)
                it.songSize = getLong(cursor, MediaStore.Audio.Media.SIZE)
                it.songArtist = getStrFromCursor(cursor, MediaStore.Audio.Artists.ARTIST)
                it.songDuration = getLong(cursor, MediaStore.Audio.Media.DURATION)
                it.songTitle = getStrFromCursor(cursor, MediaStore.Audio.Media.TITLE)
                it.songType = getStrFromCursor(cursor, MediaStore.Audio.Media.MIME_TYPE)
                it.albumId = getLong(cursor, MediaStore.Audio.Albums.ALBUM_ID)
                it.albumTitle = getStrFromCursor(cursor, MediaStore.Audio.Albums.ALBUM)
                it.albumArtist = getStrFromCursor(cursor, MediaStore.Audio.Albums.ARTIST)
                it.albumUri = ContentUris.withAppendedId(externalAlbumUri, it.albumId)
                it.albumUri = ContentUris.withAppendedId(externalAlbumUri, it.albumId)
                it.songUri = Uri.withAppendedPath(externalUri, it.songId.toString())
            }
        }

        private fun getStrFromCursor(cursor: Cursor, string: String): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(string))
        }

        private fun getLong(cursor: Cursor, string: String): Long {
            return cursor.getLong(cursor.getColumnIndexOrThrow(string))
        }
    }
}