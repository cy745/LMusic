package com.lalilu.lmusic.apis.bean

import androidx.media3.common.MediaItem
import com.blankj.utilcode.util.GsonUtils

data class ShareDto(
    val title: String,
    val artist: String,
    val coverBase64: String = ""
) {
    fun toJson(): String {
        return GsonUtils.toJson(this)
    }
}

fun MediaItem?.toShareDto(): ShareDto? {
    this ?: return null
    val title = this.mediaMetadata.title?.toString() ?: return null
    val artist = this.mediaMetadata.artist?.toString() ?: return null
    return ShareDto(title, artist)
}