package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.RecommendCardForAlbum
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.AlbumDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.ArtistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.PlaylistDetailScreen
import com.lalilu.lmusic.compose.screen.library.detail.SongDetailScreen
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
object SearchScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Search.name) {
            SearchScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Search.name
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
private fun SearchScreen(
    songsVM: SongsViewModel = getViewModel(),
    searchVM: SearchViewModel = getViewModel(),
    playingVM: PlayingViewModel = getViewModel()
) {
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val navToAlbumAction = AlbumDetailScreen.navToByArgv()
    val navToArtistAction = ArtistDetailScreen.navToByArgv()
    val navToPlaylistAction = PlaylistDetailScreen.navToByArgv()
    val navToSongsListAction = SongsScreen.navToByArgv { popUpTo(ScreenData.Search.name) }
    val navToSongAction = SongDetailScreen.navToByArgv(
        hapticType = HapticFeedbackType.LongPress
    ) {
        popUpTo(ScreenData.Search.name)
    }

    LaunchedEffect(Unit) {
        SmartBar.setExtraBar {
            com.lalilu.lmusic.compose.component.base.SearchInputBar(value = keyword, onSubmit = {
                searchVM.searchFor(keyword.value)
            })
        }
    }

    LaunchedEffect(keyword.value) {
        searchVM.searchFor(keyword.value)
    }

    SmartContainer.LazyColumn {
        searchItem(
            name = "歌曲",
            showCount = 5,
            getId = { it.id },
            items = searchVM.songsResult.value,
            getContentType = { LSong::class },
            onClickHeader = {
                if (searchVM.songsResult.value.isNotEmpty()) {
                    songsVM.updateBySongs(searchVM.songsResult.value)
                    navToSongsListAction(false.toString())
                }
            }
        ) {
            SongCard(
                modifier = Modifier.animateItemPlacement(),
                lyricRepository = playingVM.lyricRepository,
                song = { it },
                onClick = { playingVM.browser.addAndPlay(it.id) },
                onLongClick = { navToSongAction(it.id) }
            )
        }


        item(key = "AlbumHeader") {
            RecommendTitle(title = "专辑 ${searchVM.albumsResult.value.size.takeIf { it > 0 } ?: ""}")
        }
        item(key = "AlbumItems") {
            AnimatedContent(targetState = searchVM.albumsResult.value.isNotEmpty()) { show ->
                if (show) {
                    RecommendRow(
                        items = searchVM.albumsResult.value,
                        getId = { it.id }
                    ) {
                        RecommendCardForAlbum(
                            modifier = Modifier.animateItemPlacement(),
                            width = { 100.dp },
                            height = { 100.dp },
                            item = { it },
                            onClick = { navToAlbumAction(it.id) }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配专辑")
                }
            }
        }

        searchItem(
            name = "歌手",
            showCount = 5,
            getId = { it.id },
            items = searchVM.artistsResult.value,
            getContentType = { LArtist::class }
        ) {
            ArtistCard(
                artist = it,
                onClick = { navToArtistAction(it.name) }
            )
        }

        searchItem(
            name = "曲风",
            showCount = 5,
            getId = { it.id },
            items = searchVM.genresResult.value,
            getContentType = { LGenre::class }
        ) {
            Surface(onClick = {}) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .animateItemPlacement(),
                    text = it.name
                )
            }
        }

        searchItem(
            name = "歌单",
            showCount = 5,
            getId = { it.id },
            items = searchVM.playlistResult.value,
            getContentType = { LPlaylist::class }
        ) {
            Surface(onClick = { navToPlaylistAction(it.id) }) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .animateItemPlacement(),
                    text = it.name
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: LArtist,
    onClick: () -> Unit = {}
) {
    val textColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = textColor.copy(alpha = 0.4f),
            text = "#${artist._id.takeIf { it.length >= 4 }?.take(4) ?: artist._id}"
        )
        Text(
            modifier = Modifier.weight(1f),
            text = artist.name,
            fontSize = 14.sp,
            color = textColor,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${artist.requireItemsCount()} 首歌曲",
            fontSize = 12.sp,
            color = textColor.copy(0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun <I> LazyListScope.searchItem(
    name: String,
    items: List<I>,
    getId: (I) -> Any,
    showCount: Int = items.size,
    getContentType: (I) -> Any,
    onClickHeader: () -> Unit = {},
    itemContent: @Composable LazyItemScope.(I) -> Unit,
) {
    item(key = "${name}_Header") {
        RecommendTitle(
            title = "$name ${items.size.takeIf { it > 0 } ?: ""}",
            onClick = onClickHeader
        )
    }
    item(key = "${name}_EmptyTips") {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = items.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                text = "无匹配$name"
            )
        }
    }
    items(
        items = items.take(showCount),
        key = getId,
        contentType = getContentType,
        itemContent = itemContent
    )
}