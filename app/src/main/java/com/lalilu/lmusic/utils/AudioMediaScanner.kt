package com.lalilu.lmusic.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Album
import com.lalilu.lmusic.entity.Song
import java.io.File
import java.io.FileOutputStream


class AudioMediaScanner constructor(context: Context) {
    @Volatile
    private var standardDirectory: File =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    private fun saveThumbnailToSandBox(context: Context, song: Song) {
        val metadataRetriever = MediaMetadataRetriever().also {
            it.setDataSource(context, song.songUri)
        }
        val embeddedPic = metadataRetriever.embeddedPicture ?: return
        val outputStream = FileOutputStream("$standardDirectory/${song.songId}")
        outputStream.write(embeddedPic)
        outputStream.flush()
        outputStream.close()
    }

    fun loadThumbnail(song: Song): Uri {
        return Uri.fromFile(File("$standardDirectory/${song.songId}"))
    }

    fun updateSongDataBase(context: Context) {
        val songDao = MusicDatabase.getInstance(context).songDao()
        val albumDao = MusicDatabase.getInstance(context).albumDao()

        songDao.deleteAll()
        albumDao.deleteAll()

        val selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                "and ${MediaStore.Audio.Media.DURATION} >= ?" +
                "and ${MediaStore.Audio.Media.ARTIST} != ?"

        val selectionArgs =
            arrayOf(minSize.toString(), minDuration.toString(), "<unknown>")

        val cursor = context.contentResolver.query(
            externalUri, null, selection, selectionArgs, null
        )

        if (cursor!!.moveToFirst()) {
            do {
                val song = getSong(cursor)
                val album = getAlbum(song)

                if (song != null) {
                    songDao.insert(song)
                    saveThumbnailToSandBox(context, song)
                }
                if (album != null) albumDao.insert(album)
            } while (cursor.moveToNext())
        } else {
            println("[found no song]")
        }
        cursor.close()
    }


    companion object {
        private val externalUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")
        private const val minSize = 500 * 1024
        private const val minDuration = 30 * 1000

        private fun getAlbum(song: Song?): Album? {
            if (song == null) return null

            return Album().also {
                it.albumId = song.albumId
                it.albumUri = song.albumUri
                it.albumTitle = song.albumTitle
                it.albumArtist = song.albumArtist
            }
        }

        private fun getSong(cursor: Cursor): Song? {
            return Song().also {
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