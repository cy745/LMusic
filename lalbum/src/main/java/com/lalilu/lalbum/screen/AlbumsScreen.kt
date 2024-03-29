package com.lalilu.lalbum.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.LLazyVerticalStaggeredGrid
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.component.viewmodel.SongsSp
import com.lalilu.lalbum.R
import com.lalilu.lalbum.component.AlbumCard
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.Sortable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.lalilu.component.R as ComponentR

data class AlbumsScreen(
    val albumsId: List<String> = emptyList()
) : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.album_screen_title,
        icon = ComponentR.drawable.ic_album_fill
    )

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
private fun DynamicScreen.AlbumsScreen(
    title: String = "全部专辑",
    albumsSM: AlbumsScreenModel,
    playingVM: IPlayingViewModel = koinInject(),
    sortFor: String = Sortable.SORT_FOR_ALBUMS,
) {
    val albumsState = albumsSM.albums.collectAsLoadingState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigator = koinInject<GlobalNavigator>()

    LoadingScaffold(
        targetState = albumsState
    ) { albums ->
        LLazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = statusBarPadding)
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
                    isPlaying = {
                        playingVM.isItemPlaying { playing ->
                            playing.let { it as? LSong }
                                ?.let { it.album?.id == item.id }
                                ?: false
                        }
                    },
                    showTitle = { albumsSM.showTitle.value },
                    onClick = {
                        navigator.navigateTo(AlbumDetailScreen(item.id))
                    }
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