package com.lalilu.lalbum.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.LogUtils
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lalbum.component.AlbumCard
import com.lalilu.lalbum.viewModel.AlbumsEvent
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.MPlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AlbumsScreenContent(
    eventFlow: SharedFlow<AlbumsEvent> = MutableSharedFlow(),
    title: () -> String = { "" },
    albums: () -> Map<GroupIdentity, List<LAlbum>> = { emptyMap() },
    showText: () -> Boolean = { false },
) {
    val isPad = LocalWindowSize.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val gridState = rememberLazyStaggeredGridState()

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when (event) {
                is AlbumsEvent.ScrollToItem -> {
                    // TODO 待实现针对LazyVerticalStaggeredGrid的scroller
                    LogUtils.i("TODO 待实现针对LazyVerticalStaggeredGrid的scroller")
                }
            }
        }
    }

    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = StaggeredGridCells.Fixed(if (isPad) 3 else 2),
        modifier = Modifier,
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = statusBarPadding),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "Header", contentType = "Header") {
            Surface(shape = RoundedCornerShape(5.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    NavigatorHeader(
                        title = title(),
                        subTitle = "共 ${albums().size} 张专辑"
                    )
                }
            }
        }

        albums().forEach { (group, list) ->
            if (group !is GroupIdentity.None) {
                item(
                    key = group,
                    contentType = "group",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(group.text)
                }
            }

            items(
                items = list,
                key = { it.id },
                contentType = { LAlbum::class }
            ) { item ->
                AlbumCard(
                    album = { item },
                    isPlaying = { item.songs.any { MPlayer.isItemPlaying(it.id) } },
                    showTitle = showText,
                    onClick = {
                        AppRouter.intent(
                            NavIntent.Push(
                                AlbumDetailScreen(item.id)
                            )
                        )
                    }
                )
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            val padding by LocalSmartBarPadding.current

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(padding.calculateBottomPadding() + 20.dp)
            )
        }
    }
}