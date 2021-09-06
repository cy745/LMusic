package com.lalilu.media.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LPlaylist(
    // 歌单标题
    var title: String,
    // 歌单介绍
    var intro: String? = null,
    // 歌单封面
    var artUri: String? = null,
    // 歌单内歌曲
    var songs: ArrayList<LSong>? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LPlaylist

        if (title != other.title) return false
        if (intro != other.intro) return false
        if (artUri != other.artUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (intro?.hashCode() ?: 0)
        result = 31 * result + (artUri?.hashCode() ?: 0)
        return result
    }
}