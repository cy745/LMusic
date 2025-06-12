package com.lalilu.lplaylist.entity

import com.lalilu.lmedia.extension.Searchable
import kotlinx.serialization.Serializable

@Serializable
data class LPlaylist(
    val id: String,
    val title: String,
    val subTitle: String,
    val coverUri: String,
    val mediaIds: List<String>,
    val createTime: Long = System.currentTimeMillis(),
    val modifyTime: Long = System.currentTimeMillis()
) : Searchable {
    override fun getMatchSource(): String = "$title$subTitle"
}