package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.SearchInputBar
import com.lalilu.lmusic.compose.component.base.rememberSongsSelectWrapper
import com.lalilu.lmusic.compose.component.card.ArtistCard
import com.lalilu.lmusic.compose.component.card.RecommendCardForAlbum
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.extension.json
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.compose.koinInject

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    playingVM: PlayingViewModel = koinInject(),
    searchVM: SearchViewModel = koinInject(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val showSearchBar = remember { mutableStateOf(true) }
    val selectHelper = rememberSongsSelectWrapper()

    SmartBar.RegisterExtraBarContent(showSearchBar) {
        SearchInputBar(
            value = keyword,
            onSubmit = {
                searchVM.searchFor(keyword.value)
            }
        )
    }

    LaunchedEffect(keyword.value) {
        searchVM.searchFor(keyword.value)
    }

    DisposableEffect(Unit) {
        onDispose {
            context.getActivity()?.let { KeyboardUtils.hideSoftInput(it) }
        }
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
                    navigator.navigate(
                        SongsScreenDestination(
                            title = "[${keyword.value}]\n歌曲搜索结果",
                            sortFor = "SearchResult",
                            mediaIdsText = searchVM.songsResult.value.map(LSong::id).json()
                        )
                    )
                }
            }
        ) { item ->
            SongCard(
                song = { item },
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(bottom = 5.dp),
                isSelected = { selectHelper.selectedItems.any { it.id == item.id } },
                hasLyric = playingVM.lyricRepository.rememberHasLyric(song = item),
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navigator.navigate(SongDetailScreenDestination(item.id))
                },
                onEnterSelect = { selectHelper.onSelected(item) },
                isPlaying = { playingVM.isSongPlaying(mediaId = item.id) },
                onClick = {
                    if (selectHelper.isSelecting.value) {
                        selectHelper.onSelected(item)
                    } else {
                        playingVM.play(
                            mediaId = item.id,
                            addToNext = true,
                            playOrPause = true
                        )
                    }
                }
            )
        }

        val onAlbumHeaderClick = {
            if (searchVM.albumsResult.value.isNotEmpty()) {
                navigator.navigate(
                    AlbumsScreenDestination(
                        title = "[${keyword.value}]\n专辑搜索结果",
                        sortFor = "SearchResultForAlbum",
                        albumIdsText = searchVM.albumsResult.value.map(LAlbum::id).json()
                    )
                )
            }
        }

        item(key = "AlbumHeader") {
            RecommendTitle(
                title = "专辑",
                modifier = Modifier.height(64.dp),
                onClick = onAlbumHeaderClick
            ) {
                AnimatedVisibility(visible = searchVM.albumsResult.value.isNotEmpty()) {
                    Chip(
                        onClick = onAlbumHeaderClick,
                    ) {
                        Text(
                            text = "${searchVM.albumsResult.value.size} 条结果",
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
            }
        }
        item(key = "AlbumItems") {
            AnimatedContent(targetState = searchVM.albumsResult.value.isNotEmpty()) { show ->
                if (show) {
                    RecommendRow(
                        items = { searchVM.albumsResult.value },
                        getId = { it.id }
                    ) {
                        RecommendCardForAlbum(
                            modifier = Modifier.animateItemPlacement(),
                            width = { 100.dp },
                            height = { 100.dp },
                            item = { it },
                            onClick = {
                                navigator.navigate(AlbumDetailScreenDestination(albumId = it.id))
                            }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配专辑")
                }
            }
        }

        searchItem(
            name = "艺术家",
            showCount = 5,
            getId = { it.id },
            items = searchVM.artistsResult.value,
            getContentType = { LArtist::class },
            onClickHeader = {
                if (searchVM.artistsResult.value.isNotEmpty()) {
                    navigator.navigate(
                        ArtistsScreenDestination(
                            title = "[${keyword.value}]\n艺术家搜索结果",
                            sortFor = "SearchResultForArtist",
                            artistIdsText = searchVM.artistsResult.value.map(LArtist::name).json()
                        )
                    )
                }
            }
        ) {
            ArtistCard(
                artist = it,
                isPlaying = { playingVM.isArtistPlaying(it.name) },
                onClick = { navigator.navigate(ArtistDetailScreenDestination(artistName = it.name)) }
            )
        }

        searchItem(
            name = "歌单",
            showCount = 5,
            getId = { it.id },
            items = searchVM.playlistResult.value,
            getContentType = { LPlaylist::class }
        ) {
            Surface(
                onClick = {
                    navigator.navigate(PlaylistDetailScreenDestination(playlistId = it._id))
                }
            ) {
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