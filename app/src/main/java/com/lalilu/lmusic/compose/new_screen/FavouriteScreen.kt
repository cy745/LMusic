package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import com.lalilu.lmedia.repository.FavoriteRepository

@Composable
fun FavouriteScreen(
) {
    PlaylistDetailScreen(
        playlistId = FavoriteRepository.FAVORITE_PLAYLIST_ID
    )
}