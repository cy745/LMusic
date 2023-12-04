package com.lalilu.lplaylist.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.repository.PlaylistRepository

@Composable
fun replaceForFavourite(
    playlist: LPlaylist,
    callback: LPlaylist.() -> String
): String {
    if (playlist.id != PlaylistRepository.FAVOURITE_PLAYLIST_ID) {
        return playlist.callback()
    }

    return when (callback) {
        LPlaylist::title -> stringResource(id = R.string.playlist_tips_favourite)
        LPlaylist::subTitle -> stringResource(id = R.string.playlist_tips_favourite_subTitle)
        else -> playlist.callback()
    }
}