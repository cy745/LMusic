package com.lalilu.lmusic.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LPlaylist(
    var id: Long,
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

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}