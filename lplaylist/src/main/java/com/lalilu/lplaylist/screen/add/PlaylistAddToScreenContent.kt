package com.lalilu.lplaylist.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.extension.ItemSelector
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lplaylist.R
import com.lalilu.lplaylist.component.PlaylistCard
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.screen.create.PlaylistCreateOrEditScreen


@Composable
internal fun PlaylistAddToScreenContent(
    mediaIds: List<String>,
    selector: ItemSelector<LPlaylist>,
    playlists: () -> List<LPlaylist>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            NavigatorHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                title = stringResource(id = R.string.playlist_action_add_to_playlist),
                subTitle = "[S: ${mediaIds.size}] -> [P: ${selector.selected().size}]"
            ) {
                IconButton(
                    onClick = {
                        AppRouter.intent(
                            NavIntent.Push(PlaylistCreateOrEditScreen())
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(com.lalilu.component.R.drawable.ic_add_line),
                        contentDescription = null
                    )
                }
            }
        }

        items(
            items = playlists(),
            key = { it.id },
            contentType = { LPlaylist::class.java }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                isSelected = { selector.isSelected(playlist) },
                onClick = { selector.onSelect(playlist) }
            )
        }
    }
}