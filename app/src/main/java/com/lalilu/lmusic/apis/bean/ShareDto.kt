package com.lalilu.lmusic.apis.bean

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.blankj.utilcode.util.GsonUtils

data class ShareDto(
    val title: String,
    val artist: String,
    var coverBase64: String? = null,
    var coverBaseColor: Int? = null
) {
    fun toJson(): String {
        return GsonUtils.toJson(this)
    }
}

fun MediaMetadata?.toShareDto(): ShareDto? {
    this ?: return null
    val title = this.title?.toString() ?: return null
    val artist = this.artist?.toString() ?: return null
    return ShareDto(title, artist)
}

fun MediaItem?.toShareDto(): ShareDto? {
    this ?: return null
    return this.mediaMetadata.toShareDto()
}