package com.lalilu.lplaylist.entity

import java.io.Serializable

data class LPlaylist(
    val id: String,
    val title: String,
    val subTitle: String,
    val coverUri: String,
    val mediaIds: List<String>
) : Serializable