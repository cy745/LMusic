package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
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
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LPlaylist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.SearchInputBar
import com.lalilu.lmusic.compose.component.card.RecommendCardForAlbum
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination
import com.lalilu.lmusic.compose.screen.library.ArtistCard
import com.lalilu.lmusic.compose.screen.library.RecommendRow
import com.lalilu.lmusic.compose.screen.library.RecommendTitle
import com.lalilu.lmusic.compose.screen.library.searchItem
import com.lalilu.lmusic.utils.extension.getActivity
import com.lalilu.lmusic.utils.extension.idsText
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@SearchNavGraph(start = true)
@Destination
@Composable
fun SearchScreen(
    playingVM: PlayingViewModel = get(),
    searchVM: SearchViewModel = get(),
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val showSearchBar = remember { mutableStateOf(true) }

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
                            mediaIdsText = searchVM.songsResult.value.idsText()
                        )
                    )
                }
            }
        ) {
            SongCard(
                modifier = Modifier.animateItemPlacement(),
                hasLyric = playingVM.lyricRepository.rememberHasLyric(song = it),
                song = { it },
                onClick = { playingVM.browser.addAndPlay(it.id) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navigator.navigate(SongDetailScreenDestination(mediaId = it.id))
                }
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
            name = "歌手",
            showCount = 5,
            getId = { it.id },
            items = searchVM.artistsResult.value,
            getContentType = { LArtist::class }
        ) {
            ArtistCard(
                artist = it,
                onClick = {
                    navigator.navigate(ArtistDetailScreenDestination(artistName = it.name))
                }
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
