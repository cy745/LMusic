package com.lalilu.lplaylist

object PlaylistManager {
    val repository: PlaylistRepository by lazy { PlaylistRepositoryImpl() }
}