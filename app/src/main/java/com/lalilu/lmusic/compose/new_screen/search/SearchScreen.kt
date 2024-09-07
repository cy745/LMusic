package com.lalilu.lmusic.compose.new_screen.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.compose.component.base.SearchInputBar
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.search2Line
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/search")
object SearchScreen : Screen, TabScreen, ScreenBarFactory {
    private fun readResolve(): Any = SearchScreen

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.screen_title_search) },
            icon = RemixIcon.System.search2Line,
        )
    }

    @Composable
    override fun Content() {
        RegisterContent(
            isVisible = remember { mutableStateOf(true) },
            onBackPressed = null,
            content = { SearchBar() }
        )

        SearchScreen()
    }
}

@Composable
fun SearchBar(
    searchVM: SearchViewModel = singleViewModel(),
) {
    SearchInputBar(
        modifier = Modifier,
        value = searchVM.keyword,
        onValueChange = { searchVM.searchFor(it) },
        onSubmit = { searchVM.searchFor(it) }
    )
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
private fun Screen.SearchScreen(
    playingVM: PlayingViewModel = singleViewModel(),
    searchVM: SearchViewModel = singleViewModel(),
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            context.getActivity()?.let { KeyboardUtils.hideSoftInput(it) }
        }
    }

//    Songs(
//        mediaIds = searchVM.songsResult.value.take(5).map { it.mediaId },
//        sortFor = "SearchResult",
//        supportListAction = { emptyList() },
//        headerContent = {
//            item(key = "Song_Header") {
//                RecommendTitle(
//                    modifier = Modifier.height(64.dp),
//                    title = "歌曲",
//                    onClick = {
//                        if (searchVM.songsResult.value.isNotEmpty()) {
//                            AppRouter.intent(NavIntent.Push(
//                                SongsScreen(
//                                    title = "[${searchVM.keyword.value}]\n歌曲搜索结果",
//                                    mediaIds = searchVM.songsResult.value.map { it.mediaId }
//                                )
//                            ))
//                        }
//                    }
//                )
//            }
//        },
//        footerContent = {
//            val onAlbumHeaderClick = {
//                if (searchVM.albumsResult.value.isNotEmpty()) {
////                navigator.navigate(
////                    AlbumsScreenDestination(
////                        title = "[${keyword.value}]\n专辑搜索结果",
////                        sortFor = "SearchResultForAlbum",
////                        albumIdsText = searchVM.albumsResult.value.map(LAlbum::id).json()
////                    )
////                )
//                }
//            }
//
//            item(key = "AlbumHeader") {
//                RecommendTitle(
//                    title = "专辑",
//                    modifier = Modifier.height(64.dp),
//                    onClick = onAlbumHeaderClick
//                ) {
//                    AnimatedVisibility(visible = searchVM.albumsResult.value.isNotEmpty()) {
//                        Chip(
//                            onClick = onAlbumHeaderClick,
//                        ) {
//                            Text(
//                                text = "${searchVM.albumsResult.value.size} 条结果",
//                                style = MaterialTheme.typography.caption,
//                            )
//                        }
//                    }
//                }
//            }
//            item(key = "AlbumItems") {
//                AnimatedContent(
//                    targetState = searchVM.albumsResult.value.isNotEmpty(),
//                    label = ""
//                ) { show ->
//                    if (show) {
//                        RecommendRow(
//                            items = { searchVM.albumsResult.value },
//                            getId = { it.id }
//                        ) {
//                            RecommendCardForAlbum(
//                                modifier = Modifier.animateItemPlacement(),
//                                width = { 100.dp },
//                                height = { 100.dp },
//                                item = { it },
//                                onClick = {
////                                navigator.navigate(AlbumDetailScreenDestination(albumId = it.id))
//                                }
//                            )
//                        }
//                    } else {
//                        Text(modifier = Modifier.padding(20.dp), text = "无匹配专辑")
//                    }
//                }
//            }
//
//            searchItem(
//                name = "艺术家",
//                showCount = 5,
//                getId = { it.id },
//                items = searchVM.artistsResult.value,
//                getContentType = { LArtist::class },
//                onClickHeader = {
//                    if (searchVM.artistsResult.value.isNotEmpty()) {
////                    navigator.navigate(
////                        ArtistsScreenDestination(
////                            title = "[${keyword.value}]\n艺术家搜索结果",
////                            sortFor = "SearchResultForArtist",
////                            artistIdsText = searchVM.artistsResult.value.map(LArtist::name).json()
////                        )
////                    )
//                    }
//                }
//            ) { item ->
//                ArtistCard(
//                    artist = item,
//                    isPlaying = {
//                        playingVM.isItemPlaying { playing ->
//                            playing.let { it as? LSong }
//                                ?.let { song -> song.artists.any { it.name == item.name } }
//                                ?: false
//                        }
//                    },
//                    onClick = {
////                    navigator.navigate(ArtistDetailScreenDestination(artistName = item.name))
//                    }
//                )
//            }
//        }
//    )
}

@OptIn(ExperimentalMaterialApi::class)
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
            modifier = Modifier.height(64.dp),
            title = name,
            onClick = onClickHeader
        ) {
            AnimatedVisibility(visible = items.isNotEmpty()) {
                Chip(
                    onClick = onClickHeader,
                ) {
                    Text(
                        text = "${items.size} 条结果",
                        style = MaterialTheme.typography.caption,
                    )
                }
            }
        }
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