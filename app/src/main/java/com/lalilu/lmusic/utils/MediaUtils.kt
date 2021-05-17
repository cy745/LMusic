package com.lalilu.lmusic.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Album
import com.lalilu.lmusic.entity.Song

class MediaUtils {
    companion object {
        private val externalUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")

        fun updateSongDataBase(context: Context) {
            val songDao = MusicDatabase.getInstance(context).songDao()
            val albumDao = MusicDatabase.getInstance(context).albumDao()

            songDao.deleteAll()
            albumDao.deleteAll()

            val cursor = context.contentResolver.query(
                externalUri, null,
                null, null,
                null, null
            )
            if (cursor!!.moveToFirst()) {
                do {
                    val song = getSong(cursor)
                    val album = getAlbum(song)

                    if (song != null) songDao.insert(song)
                    if (album != null) albumDao.insert(album)

                } while (cursor.moveToNext())
            } else {
                println("[found no song]")
            }
            cursor.close()
        }

        private fun getSong(cursor: Cursor): Song? {
            val songSize = getLong(cursor, MediaStore.Audio.Media.SIZE)
            val songDuration = getLong(cursor, MediaStore.Audio.Media.DURATION)
            val artist = getStrFromCursor(cursor, MediaStore.Audio.Media.ARTIST)
            if (songSize < 500 * 1024
                || songDuration < 30 * 1000
                || artist == "<unknown>"
            ) return null

            return Song().also {
                it.duration = songDuration
                it.songSize = songSize
                it.artist = artist
                it.songTitle = getStrFromCursor(cursor, MediaStore.Audio.Media.TITLE)
                it.songId = getLong(cursor, MediaStore.Audio.Media._ID)
                it.songUri = Uri.withAppendedPath(externalUri, it.songId.toString())
                it.albumId = getLong(cursor, MediaStore.Audio.Albums.ALBUM_ID)
                it.albumTitle = getStrFromCursor(cursor, MediaStore.Audio.Albums.ALBUM)
                it.albumArtist =
                    getStrFromCursor(cursor, MediaStore.Audio.AudioColumns.ALBUM_ARTIST)
                it.albumUri = ContentUris.withAppendedId(externalAlbumUri, it.albumId)
            }
        }

        private fun getAlbum(song: Song?): Album? {
            if (song == null) return null

            return Album().also {
                it.albumId = song.albumId
                it.albumUri = song.albumUri
                it.albumTitle = song.albumTitle
                it.albumArtist = song.albumArtist
            }
        }

        private fun getStrFromCursor(cursor: Cursor, string: String): String {
            return cursor.getString(cursor.getColumnIndex(string))
        }

        private fun getLong(cursor: Cursor, string: String): Long {
            return cursor.getLong(cursor.getColumnIndex(string))
        }
    }
}