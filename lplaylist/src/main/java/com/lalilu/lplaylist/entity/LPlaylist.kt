package com.lalilu.lplaylist.entity

import com.lalilu.lmedia.extension.Searchable
import java.io.Serializable
import kotlin.String

data class LPlaylist(
    val id: String,
    val title: String,
    val subTitle: String,
    val coverUri: String,
    val mediaIds: List<String>,
    val createTime: Long = System.currentTimeMillis(),
    val modifyTime: Long = System.currentTimeMillis()
) : Serializable, Searchable {
    override fun getMatchSource(): String = "$title$subTitle"
}