package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
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
import com.lalilu.lmusic.viewmodel.LocalPlayingVM
import com.lalilu.lmusic.viewmodel.LocalSearchVM
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel

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
    searchVM: SearchViewModel = LocalSearchVM.current,
    playingVM: PlayingViewModel = LocalPlayingVM.current
) {
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val navToAlbumAction = AlbumDetailScreen.navToByArgv()
    val navToArtistAction = ArtistDetailScreen.navToByArgv()
    val navToPlaylistAction = PlaylistDetailScreen.navToByArgv()
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
            items = searchVM.songsResult.value.take(5),
            getId = { it.id },
            getContentType = { LSong::class }
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
            AnimatedContent(targetState = searchVM.albumsResult.value.isNotEmpty()) {
                if (it) {
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
            items = searchVM.artistsResult.value.take(5),
            getId = { it.id },
            getContentType = { LArtist::class }
        ) {
            Surface(onClick = { navToArtistAction(it.name) }) {
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
            name = "曲风",
            items = searchVM.genresResult.value.take(5),
            getId = { it.id },
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
            items = searchVM.playlistResult.value.take(5),
            getId = { it.id },
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

fun <I> LazyListScope.searchItem(
    name: String,
    items: List<I>,
    getId: (I) -> Any,
    getContentType: (I) -> Any,
    itemContent: @Composable LazyItemScope.(I) -> Unit,
) {
    item(key = "${name}_Header") {
        RecommendTitle(title = "$name ${items.size.takeIf { it > 0 } ?: ""}", onClick = { })
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
    items(items = items, key = getId, contentType = getContentType, itemContent = itemContent)
}