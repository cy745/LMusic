package com.lalilu.lmusic.utils

import android.database.Cursor
import android.provider.MediaStore


fun Cursor.getSongId(): Long {
    return this.getLong(this.getColumnIndex(MediaStore.Audio.Media._ID))
}

fun Cursor.getSongTitle(): String {
    return this.getString(this.getColumnIndex(MediaStore.Audio.Media.TITLE))
}

fun Cursor.getAlbumId(): Long {
    return this.getLong(this.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
}

fun Cursor.getAlbumTitle(): String {
    return this.getString(this.getColumnIndex(MediaStore.Audio.Media.ALBUM))
}

fun Cursor.getArtist(): String {
    return this.getString(this.getColumnIndex(MediaStore.Audio.Media.ARTIST))
}

fun Cursor.getArtists(): List<String> {
    return this.getArtist().split("/")
}

fun Cursor.getSongSize(): Long {
    return this.getLong(this.getColumnIndex(MediaStore.Audio.Media.SIZE))
}

fun Cursor.getSongData(): String {
    return this.getString(this.getColumnIndex(MediaStore.Audio.Media.DATA))
}

fun Cursor.getSongDuration(): Long {
    return this.getLong(this.getColumnIndex(MediaStore.Audio.Media.DURATION))
}

fun Cursor.getSongMimeType(): String {
    return this.getString(this.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE))
}