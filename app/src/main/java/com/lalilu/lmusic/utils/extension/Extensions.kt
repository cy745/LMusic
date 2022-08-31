package com.lalilu.lmusic.utils.extension

import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import kotlin.math.roundToInt

fun Drawable.toBitmap(): Bitmap {
    val w = this.intrinsicWidth
    val h = this.intrinsicHeight

    val config = Bitmap.Config.ARGB_8888
    val bitmap = Bitmap.createBitmap(w, h, config)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, w, h)
    this.draw(canvas)
    return bitmap
}

fun Bitmap.addShadow(
    fromColor: Int = Color.argb(55, 0, 0, 0),
    toColor: Int = Color.TRANSPARENT,
    percent: Float = 0.25f,
    orientation: List<GradientDrawable.Orientation> = listOf(TOP_BOTTOM, BOTTOM_TOP)
): Bitmap {
    orientation.forEach {
        val mBackShadowColors = intArrayOf(fromColor, toColor)
        val mBackShadowDrawableLR = GradientDrawable(it, mBackShadowColors)
        val bound = Rect(0, 0, width, height)
        val percentHeight = (height * percent).roundToInt()
        val percentWidth = (width * percent).roundToInt()

        when (it) {
            TOP_BOTTOM -> bound.set(0, 0, width, percentHeight)
            RIGHT_LEFT -> bound.set(0, 0, percentWidth, height)
            BOTTOM_TOP -> bound.set(0, height - percentHeight, width, height)
            LEFT_RIGHT -> bound.set(width - percentWidth, 0, width, height)
            else -> {}
        }
        mBackShadowDrawableLR.bounds = bound
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
        mBackShadowDrawableLR.draw(Canvas(this))
    }
    return this
}

fun <T, K> List<T>.moveHeadToTailWithSearch(id: K, checkIsSame: (T, K) -> Boolean): MutableList<T> {
    val size = this.indexOfFirst { checkIsSame(it, id) }
    if (size <= 0) return this.toMutableList()
    return this.moveHeadToTail(size)
}

fun <T> List<T>.moveHeadToTail(size: Int): MutableList<T> {
    val temp = this.take(size).toMutableList()
    temp.addAll(0, this.drop(size))
    return temp
}

fun Cursor.getSongId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media._ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.TITLE)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getAlbumId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getAlbumTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtistId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
    return if (index < 0) 0 else this.getLong(index)
}

fun Cursor.getArtist(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ARTIST)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtists(): List<String> {
    return this.getArtist().split("/")
}

fun Cursor.getSongSize(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.SIZE)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongData(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DATA)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getSongDuration(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DURATION)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongMimeType(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
    return if (index < 0) "" else this.getString(index)
}

@RequiresApi(Build.VERSION_CODES.R)
fun Cursor.getSongGenre(): String? {
    val index = this.getColumnIndex(MediaStore.Audio.AudioColumns.GENRE)
    return if (index == -1) null else this.getString(index)
}

fun Cursor.getAlbumArt(): Uri {
    return ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart/"),
        getAlbumId()
    )
}

fun Cursor.getMediaUri(): Uri {
    return Uri.withAppendedPath(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        getSongId().toString()
    )
}

fun MediaItem.updateArtworkUri(uri: Uri?): MediaItem {
    uri ?: return this
    return buildUpon().setMediaMetadata(
        mediaMetadata.buildUpon()
            .setArtworkUri(uri)
            .build()
    ).build()
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

fun <T> List<T>.getNextOf(item: T, cycle: Boolean = false): T? {
    val nextIndex = indexOf(item) + 1
    return getOrNull(if (cycle) nextIndex % size else nextIndex)
}


fun <T> List<T>.getPreviousOf(item: T, cycle: Boolean = false): T? {
    var previousIndex = indexOf(item) - 1
    if (previousIndex < 0 && cycle) {
        previousIndex = size - 1
    }
    return getOrNull(previousIndex)
}