package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.viewmodel.LibraryViewModel

@Composable
fun ArtistScreen(
    libraryViewModel: LibraryViewModel
) {
    val artists by libraryViewModel.artists.observeAsState(emptyList())
    val navController = LocalNavigatorHost.current
    var sortByState by rememberDataSaverState("KEY_SORT_BY_ArtistScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_ArtistScreen", true)

    val onArtistSelected = remember {
        { artistId: String ->
            navController.navigate("${MainScreenData.ArtistsDetail.name}/$artistId")
        }
    }

    SmartContainer.LazyColumn {
        artists.forEachIndexed { index, item ->
            item {
                ArtistCard(
                    index = index,
                    artistName = item.name,
                    songCount = item.songs.size,
                    onClick = { onArtistSelected(item.name) }
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    index: Int,
    artistName: String,
    songCount: Int,
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(
                start = 10.dp,
                end = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.DarkGray,
                text = "${index + 1}"
            )
            Text(
                text = artistName,
                fontSize = 14.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "$songCount 首歌曲",
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}