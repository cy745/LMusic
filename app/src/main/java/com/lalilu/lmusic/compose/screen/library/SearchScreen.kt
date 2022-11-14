package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
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
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.viewmodel.SearchViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SearchScreen(
    searchVM: SearchViewModel = hiltViewModel()
) {
    val songsResult = searchVM.songsResult.observeAsState(initial = emptyList())
    val artistsResult = searchVM.artistsResult.observeAsState(initial = emptyList())
    val albumsResult = searchVM.albumsResult.observeAsState(initial = emptyList())
    val genresResult = searchVM.genresResult.observeAsState(initial = emptyList())
    val playlistResult = searchVM.playlistResult.observeAsState(initial = emptyList())

    val keyword = remember { mutableStateOf(searchVM.keywordStr.value) }

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
            RecommendTitle("歌曲 ${songsResult.value.size.takeIf { it > 0 } ?: ""}", onClick = { })
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
                            onClick = { }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配歌曲")
                }
            }
        }
        stickyHeader(key = "AlbumHeader") {
            RecommendTitle("专辑 ${albumsResult.value.size.takeIf { it > 0 } ?: ""}")
        }
        item {
            AnimatedContent(targetState = albumsResult.value.isNotEmpty()) {
                if (it) {
                    RecommendRow(
                        items = albumsResult.value,
                        getId = { it.id }
                    ) {
                        AlbumCard(
                            modifier = Modifier.animateItemPlacement(),
                            album = { it }
                        )
                    }
                } else {
                    Text(modifier = Modifier.padding(20.dp), text = "无匹配专辑")
                }
            }
        }

        genresResult.value.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("曲风 ${it.size}")
                RecommendRow(
                    scrollToStartWhenUpdate = true,
                    items = it,
                    getId = { it.id }
                ) {
                    Text(
                        modifier = Modifier.animateItemPlacement(),
                        text = it.name
                    )
                }
            }
        }

        playlistResult.value.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("歌单 ${it.size}")
                RecommendRow(
                    scrollToStartWhenUpdate = true,
                    items = it,
                    getId = { it.id }
                ) {
                    Text(
                        modifier = Modifier.animateItemPlacement(),
                        text = it.name
                    )
                }
            }
        }
    }
}