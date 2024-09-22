package com.lalilu.lalbum.screen.albums

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lalbum.component.AlbumCard
import com.lalilu.lalbum.screen.detail.AlbumDetailScreen
import com.lalilu.lalbum.viewModel.AlbumsSM
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import org.koin.compose.koinInject


@Composable
internal fun AlbumsScreenContent(
    title: String = "全部专辑",
    albumsSM: AlbumsSM,
    playingVM: IPlayingViewModel = koinInject(),
) {
    val isPad = LocalWindowSize.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val albums by albumsSM.albums

    LazyVerticalStaggeredGrid(
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
                        title = title,
                        subTitle = "共 ${albums.size} 张专辑"
                    )
                }
            }
        }

        albums.forEach { (group, list) ->
            items(
                items = list,
                key = { it.id },
                contentType = { LAlbum::class }
            ) { item ->
                AlbumCard(
                    album = { item },
                    isPlaying = {
                        playingVM.isItemPlaying { playing ->
                            playing.let { it as? LSong }
                                ?.let { it.album?.id == item.id }
                                ?: false
                        }
                    },
                    showTitle = { albumsSM.showTitle.value },
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