package com.lalilu.lmusic.compose.screen.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.RemixIcon
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.smartBarPadding
import com.lalilu.lmusic.compose.screen.search.extensions.SearchArtistsResult
import com.lalilu.lmusic.compose.screen.search.extensions.SearchSongsResult
import com.lalilu.lmusic.viewmodel.SearchScreenState
import com.lalilu.lmusic.viewmodel.SearchVM
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.search2Line
import com.lalilu.remixicon.system.searchLine
import com.zhangke.krouter.annotation.Destination
import org.koin.compose.koinInject

@Destination("/pages/search")
data object SearchScreen : Screen, TabScreen, ScreenInfoFactory, ScreenBarFactory {
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
        val searchVM: SearchVM = koinInject()

        SearchBar(searchVM = searchVM)

        SearchScreenContent(
            searchVM = searchVM
        )
    }
}

@Composable
private fun SearchScreenContent(
    searchVM: SearchVM = koinInject(),
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val statusBar = WindowInsets.statusBars.asPaddingValues()
    val state = searchVM.searchState.value

    DisposableEffect(Unit) {
        onDispose { keyboard?.hide() }
    }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = when {
            state is SearchScreenState.Idle -> "Idle"
            state is SearchScreenState.Empty -> "Empty"
            else -> "Searching"
        },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = ""
    ) { searchState ->
        if (searchState == "Idle") {
            SearchTips(
                modifier = Modifier
                    .fillMaxSize()
            )
            return@AnimatedContent
        }

        if (searchState == "Empty") {
            SearchTips(
                modifier = Modifier
                    .fillMaxSize(),
                title = "暂无搜索结果"
            )
            return@AnimatedContent
        }

        val songsResult = remember {
            SearchSongsResult {
                (searchVM.searchState.value as? SearchScreenState.Searching)
                    ?.songs ?: emptyList()
            }
        }.register()

        val artistsResult = remember {
            SearchArtistsResult {
                (searchVM.searchState.value as? SearchScreenState.Searching)
                    ?.artists ?: emptyList()
            }
        }.register()

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = statusBar,
            columns = GridCells.Fixed(6)
        ) {
            songsResult(this)
            artistsResult(this)
            smartBarPadding()
        }
    }
}

@Preview
@Composable
fun SearchTips(
    modifier: Modifier = Modifier,
    title: String = "搜索曲库内所有内容"
) {
    val paddingBottom = LocalSmartBarPadding.current.value.calculateBottomPadding()
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = paddingBottom + imePadding),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = RemixIcon.System.searchLine,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground.copy(0.4f)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colors.onBackground.copy(0.6f)
                )
            }
        }
    }
}