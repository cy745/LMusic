package com.lalilu.lmusic.compose.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.viewmodel.SearchViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
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
        songsResult.value.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("歌曲 ${it.size}", onClick = { })
                RecommendRow(
                    items = it,
                    getId = { it.id }
                ) {
                    RecommendCard(
                        modifier = Modifier.animateItemPlacement(),
                        item = { it },
                        onClick = { }
                    )
                }
            }
        }

        artistsResult.value.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("歌手 ${it.size}", onClick = { })
                RecommendRow(
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

        albumsResult.value.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("专辑 ${it.size}")
                RecommendRow(
                    scrollToStartWhenUpdate = true,
                    items = it,
                    getId = { it.id }
                ) {
                    AlbumCard(
                        modifier = Modifier.animateItemPlacement(),
                        album = { it }
                    )
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