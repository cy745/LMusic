package com.lalilu.lmusic.compose.new_screen

import androidx.compose.runtime.Composable
import com.lalilu.lmedia.repository.FavoriteRepository
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@FavouriteNavGraph(start = true)
@Destination
@Composable
fun FavouriteScreen(
    navigator: DestinationsNavigator
) {
    PlaylistDetailScreen(
        playlistId = FavoriteRepository.FAVORITE_PLAYLIST_ID,
        navigator = navigator
    )
}