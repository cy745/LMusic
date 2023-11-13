package com.lalilu.lplaylist.screen

import androidx.compose.runtime.Composable
import com.lalilu.component.Songs
import com.lalilu.component.base.DynamicScreen
import com.lalilu.lplaylist.repository.PlaylistSp
import org.koin.compose.koinInject

data class PlaylistDetailScreen(
    val playlistId: String
) : DynamicScreen() {

    @Composable
    override fun Content() {
        PlaylistDetailScreen(playlistId)
    }
}

@Composable
private fun DynamicScreen.PlaylistDetailScreen(
    playlistId: String,
    sp: PlaylistSp = koinInject()
) {
    val mediaIds = sp.obtainList<String>("PlaylistDetail_${playlistId}", autoSave = false)

    Songs(
        mediaIds = mediaIds.value,
        supportListAction = { emptyList() },
    ) {

    }
}