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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.RemixIcon
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.component.viewmodel.SongsSp
import com.lalilu.lalbum.R
import com.lalilu.lalbum.component.AlbumCard
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lplayer.MPlayer
import com.lalilu.remixicon.Media
import com.lalilu.remixicon.media.albumFill
import com.zhangke.krouter.annotation.Destination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Destination("/pages/albums")
data class AlbumsScreen(
    val albumsId: List<String> = emptyList()
) : Screen, ScreenInfoFactory {
    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = { stringResource(id = R.string.album_screen_title) },
            icon = RemixIcon.Media.albumFill
        )
    }

    @Composable
    override fun Content() {
        val albumsSM = getScreenModel<AlbumsScreenModel>()

        LaunchedEffect(Unit) {
            albumsSM.updateAlbumsId(albumsId)
        }

        AlbumsScreen(
            albumsSM = albumsSM,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsScreenModel(
    sp: SongsSp
) : ScreenModel {
    private val albumsId = MutableStateFlow<List<String>>(emptyList())
    val showTitle = sp.obtain<Boolean>("test")
    val albums = albumsId.flatMapLatest {
        if (it.isEmpty()) LMedia.getFlow<LAlbum>()
        else LMedia.flowMapBy<LAlbum>(it)
    }

    fun updateAlbumsId(albumsId: List<String>) = screenModelScope.launch {
        this@AlbumsScreenModel.albumsId.emit(albumsId)
    }
}

@Composable
private fun AlbumsScreen(
    title: String = "全部专辑",
    albumsSM: AlbumsScreenModel,
    playingVM: IPlayingViewModel = koinInject(),
) {
    val isPad = LocalWindowSize.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val albumsState = albumsSM.albums.collectAsLoadingState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LoadingScaffold(
        targetState = albumsState
    ) { albums ->
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

            items(
                items = albums,
                key = { it.id },
                contentType = { LAlbum::class }
            ) { item ->
                AlbumCard(
                    album = { item },
                    isPlaying = { item.songs.any { MPlayer.isItemPlaying(it.id) } },
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

//    val scrollProgress = remember(gridState) {
//        derivedStateOf {
//            if (gridState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
//            gridState.firstVisibleItemIndex / gridState.layoutInfo.totalItemsCount.toFloat()
//        }
//    }
//
////    LaunchedEffect(albumIdsText) {
////        albumsVM.updateByIds(
////            ids = albumIdsText.getIds(),
////            sortFor = sortFor,
////            supportSortRules = supportSortRules,
////            supportGroupRules = supportGroupRules,
////            supportOrderRules = supportOrderRules
////        )
////    }
//
//    SortPanelWrapper(
//        sortFor = sortFor,
//        showPanelState = showSortPanel,
//        supportListAction = { emptyList() },
//        sp = koinInject<SettingsSp>()
//    ) {
//    }
}