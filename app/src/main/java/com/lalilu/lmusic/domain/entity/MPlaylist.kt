package com.lalilu.lmusic.domain.entity

import android.net.Uri

data class MPlaylist(

    val playlistId: Long,

    val playlistTitle: String = "New Playlist.",

    var playlistCoverUri: Uri = Uri.EMPTY,

    val playlistInfo: String = "empty.",
)