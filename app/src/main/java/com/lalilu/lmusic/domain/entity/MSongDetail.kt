package com.lalilu.lmusic.domain.entity

import android.net.Uri

data class MSongDetail(
    val songId: Long,

    val songLyric: String = "",

    val songSize: Long = 0,

    var songCoverUri: Uri = Uri.EMPTY,

    val songData: String = ""
)