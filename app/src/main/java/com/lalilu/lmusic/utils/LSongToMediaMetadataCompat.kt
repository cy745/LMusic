package com.lalilu.lmusic.utils

import android.support.v4.media.MediaMetadataCompat
import android.text.TextUtils
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.domain.entity.LSong

@Deprecated("后期删除")
fun LSong.toMediaMetaData(): MediaMetadataCompat {
    val metadata = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.mTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.mArtist.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this.mAlbum?.albumTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.mId.toString())
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.mLocalInfo?.mDuration ?: 0L)
        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, this.mArtUri.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.mLocalInfo?.mData)
        .putString(Config.MEDIA_MIME_TYPE, this.mLocalInfo?.mMimeType)

    if (this.mArtist != null) {
        metadata.putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            TextUtils.join(" / ", this.mArtist!!.map { it.name })
        )
    }
    if (this.mArtUri != null) {
        metadata.putBitmap(
            MediaMetadataCompat.METADATA_KEY_ART,
            BitmapUtils.loadBitmapFromUri(this.mArtUri!!, 500)
        )
    }
    return metadata.build()
}