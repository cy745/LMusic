package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendCardForAlbum
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SearchViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchScreen(
    searchVM: SearchViewModel = hiltViewModel(),
    playingVM: PlayingViewModel = hiltViewModel()
) {
    val songsResult = searchVM.songsResult.observeAsState(initial = emptyList())
    val artistsResult = searchVM.artistsResult.observeAsState(initial = emptyList())
    val albumsResult = searchVM.albumsResult.observeAsState(initial = emptyList())
    val genresResult = searchVM.genresResult.observeAsState(initial = emptyList())
    val playlistResult = searchVM.playlistResult.observeAsState(initial = emptyList())
    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }
    val navToSongAction = ScreenActions.navToSongById(popUpToRoute = ScreenData.Search)
    val navToAlbumAction = ScreenActions.navToAlbumById()

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
        stickyHeader(key = "SongsHeader") {
            RecommendTitle(
                modifier = Modifier.statusBarsPadding(),
                title = "歌曲 ${songsResult.value.size.takeIf { it > 0 } ?: ""}",
                onClick = { })
        }
        item {
            AnimatedContent(targetState = songsResult.value.isNotEmpty()) {
                if (it) {
                    RecommendRow(
                        items = songsResult.value,
                        getId = { it.id }
                    ) {
                        RecommendCard(
                            modifier = Modifier.animateItemPlacement(),
                            item = { it },
                            width = { 100.dp },
                            height = { 100.dp },
                            onClick = { navToSongAction(it.id) },
                            onClickButton = { playingVM.playOrPauseSong(mediaId = it.id) },
                            isPlaying = { playingVM.isSongPlaying(mediaId = it.id) }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配歌曲")
                }
            }
        }
        stickyHeader(key = "AlbumHeader") {
            RecommendTitle(
                modifier = Modifier.statusBarsPadding(),
                title = "专辑 ${albumsResult.value.size.takeIf { it > 0 } ?: ""}")
        }
        item {
            AnimatedContent(targetState = albumsResult.value.isNotEmpty()) {
                if (it) {
                    RecommendRow(
                        items = albumsResult.value,
                        getId = { it.id }
                    ) {
                        RecommendCardForAlbum(
                            modifier = Modifier.animateItemPlacement(),
                            width = { 100.dp },
                            height = { 100.dp },
                            item = { it },
                            onClick = { navToAlbumAction(it.id) },
                            isPlaying = { playingVM.isSongPlaying(mediaId = it.id) }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配专辑")
                }
            }
        }
        stickyHeader(key = "ArtistsHeader") {
            RecommendTitle(
                modifier = Modifier.statusBarsPadding(),
                title = "歌手 ${artistsResult.value.size.takeIf { it > 0 } ?: ""}"
            )
        }
        item {
            AnimatedContent(targetState = artistsResult.value.isNotEmpty()) {
                if (it) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        artistsResult.value.forEach {
                            Text(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .animateItemPlacement(),
                                text = it.name
                            )
                        }
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配歌手")
                }
            }
        }

        stickyHeader(key = "GenresHeader") {
            RecommendTitle(
                modifier = Modifier.statusBarsPadding(),
                title = "曲风 ${genresResult.value.size.takeIf { it > 0 } ?: ""}"
            )
        }
        item {
            AnimatedContent(targetState = genresResult.value.isNotEmpty()) {
                if (it) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        genresResult.value.forEach {
                            Text(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .animateItemPlacement(),
                                text = it.name
                            )
                        }
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配曲风")
                }
            }
        }


        stickyHeader(key = "PlaylistsHeader") {
            RecommendTitle(
                modifier = Modifier.statusBarsPadding(),
                title = "歌单 ${playlistResult.value.size.takeIf { it > 0 } ?: ""}"
            )
        }
        item {
            AnimatedContent(targetState = playlistResult.value.isNotEmpty()) {
                if (it) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        playlistResult.value.forEach {
                            Text(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .animateItemPlacement(),
                                text = it.name
                            )
                        }
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配歌单")
                }
            }
        }
    }
}