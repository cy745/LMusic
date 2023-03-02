package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.ArtistDetailScreen
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalAnimationApi::class)
object ArtistScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Artists.name) {
            ArtistScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Artists.name
    }
}

@Composable
private fun ArtistScreen(
    libraryVM: LibraryViewModel = get()
) {
    val navToArtistAction = ArtistDetailScreen.navToByArgv()
    val artists by libraryVM.artists

    SmartContainer.LazyColumn {
        itemsIndexed(items = artists) { index, item ->
            ArtistCard(
                index = index,
                artistName = item.name,
                songCount = item.requireItemsCount(),
                onClick = { navToArtistAction.invoke(item.name) }
            )
        }
    }
}

@Composable
fun ArtistCard(
    modifier: Modifier = Modifier,
    index: Int,
    artistName: String,
    songCount: Long,
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = modifier
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