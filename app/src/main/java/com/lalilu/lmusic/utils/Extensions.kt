package com.lalilu.lmusic.utils

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.*
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.palette.graphics.Palette
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.MEDIA_MEDIA_DATA
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.toEmbeddedCoverSource
import kotlin.math.roundToInt

fun Palette?.getAutomaticColor(): Int {
    if (this == null) return Color.DKGRAY
    var oldColor = this.getDarkVibrantColor(Color.LTGRAY)
    if (ColorUtils.isLightColor(oldColor))
        oldColor = this.getDarkMutedColor(Color.LTGRAY)
    return oldColor
}

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
    fromColor: Int, toColor: Int, percent: Float,
    orientation: GradientDrawable.Orientation
): Bitmap {
    val mBackShadowColors = intArrayOf(fromColor, toColor)
    val mBackShadowDrawableLR = GradientDrawable(orientation, mBackShadowColors)
    val bound = Rect(0, 0, width, height)
    val percentHeight = (height * percent).roundToInt()
    val percentWidth = (width * percent).roundToInt()

    when (orientation) {
        TOP_BOTTOM -> bound.set(0, 0, width, percentHeight)
        RIGHT_LEFT -> bound.set(0, 0, percentWidth, height)
        BOTTOM_TOP -> bound.set(0, height - percentHeight, width, height)
        LEFT_RIGHT -> bound.set(width - percentWidth, 0, width, height)
        else -> {}
    }
    mBackShadowDrawableLR.bounds = bound
    mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    mBackShadowDrawableLR.draw(Canvas(this))
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

fun Cursor.getSongGenre(): String {
    val index = this.getColumnIndex(MediaStore.EXTRA_MEDIA_GENRE)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getMediaUri(): String {
    return Uri.withAppendedPath(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        getSongId().toString()
    ).toString()
}

fun MSong.toSimpleMetadata(context: Context): MediaMetadataCompat {
    val metadata = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.songTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.showingArtist)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this.albumTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.songId.toString())
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.songDuration)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.songUri.toString())
        .putString(Config.MEDIA_MIME_TYPE, this.songMimeType)
        .putString(Config.MEDIA_MEDIA_DATA, this.songData)

    val bitmap = context.imageLoader.executeBlocking(
        ImageRequest.Builder(context)
            .allowHardware(false)
            .data(this.songUri.toEmbeddedCoverSource())
            .build()
    ).drawable?.toBitmap()
    metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
    return metadata.build()
}

//fun MSong.toSimpleMetadata(): MediaMetadataCompat {
//    val metadata = MediaMetadataCompat.Builder()
//        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.songTitle)
//        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.showingArtist)
//        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this.albumTitle)
//        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.songId.toString())
//        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.songDuration)
//        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.songUri.toString())
//        .putString(Config.MEDIA_MIME_TYPE, this.songMimeType)
//    return metadata.build()
//}

//fun MediaMetadataCompat.toMediaItem(): MediaBrowserCompat.MediaItem {
//    return MediaBrowserCompat.MediaItem(
//        MediaDescriptionCompat.Builder()
//            .setTitle(this.description.title)
//            .setMediaId(this.description.mediaId)
//            .setSubtitle(this.description.subtitle)
//            .setDescription(this.description.description)
//            .setIconUri(this.description.iconUri)
//            .setMediaUri(this.description.mediaUri)
//            .setExtras(this.bundle).build(),
//        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//    )
//}

fun MediaMetadataCompat.saveTo(pref: SharedPreferences) {
    with(pref.edit()) {
        this.putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            this@saveTo.description.mediaId
        )
        this.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            this@saveTo.description.title.toString()
        )
        this.putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
            this@saveTo.description.mediaUri.toString()
        )
        this.apply()
    }
}

fun PlaybackStateCompat.saveTo(pref: SharedPreferences) {
    with(pref.edit()) {
        this.putLong(Config.LAST_POSITION, this@saveTo.position)
        this.apply()
    }
}

fun SharedPreferences.getLastPlaybackState(): PlaybackStateCompat {
    return PlaybackStateCompat.Builder()
        .setState(
            PlaybackStateCompat.STATE_STOPPED,
            this.getLong(Config.LAST_POSITION, 0L),
            1.0f
        ).build()
}

fun SharedPreferences.getLastMediaMetadata(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .moveStringData(this, MediaMetadataCompat.METADATA_KEY_TITLE)
        .moveStringData(this, MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        .moveStringData(this, MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
        .moveStringData(this, MEDIA_MEDIA_DATA)
        .build()
}

fun MediaMetadataCompat.Builder.moveStringData(
    from: SharedPreferences,
    key: String,
    defaultValue: String? = ""
): MediaMetadataCompat.Builder {
    putString(key, from.getString(key, defaultValue))
    return this
}

