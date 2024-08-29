package com.lalilu.lalbum.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.lalbum.R
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.zhangke.krouter.annotation.Destination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@Destination("/pages/albums/detail")
data class AlbumDetailScreen(
    private val albumId: String
) : Screen, ScreenInfoFactory, ScreenType.List {
    override val key: ScreenKey = "${super.key}:$albumId"

    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(title = R.string.album_screen_title)
    }

    @Composable
    override fun Content() {
        val albumDetailSM = getScreenModel<AlbumDetailScreenModel>()

        LaunchedEffect(Unit) {
            albumDetailSM.updateAlbumId(albumId)
        }

        AlbumDetail(albumDetailSM = albumDetailSM)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailScreenModel : ScreenModel {
    private val albumId = MutableStateFlow<String?>(null)
    val album = albumId.flatMapLatest { LMedia.getFlow<LAlbum>(it) }

    fun updateAlbumId(albumId: String) = screenModelScope.launch {
        this@AlbumDetailScreenModel.albumId.emit(albumId)
    }
}

@Composable
private fun Screen.AlbumDetail(
    albumDetailSM: AlbumDetailScreenModel
) {
    val albumLoadingState = albumDetailSM.album.collectAsLoadingState()

//    LoadingScaffold(
//        modifier = Modifier.fillMaxSize(),
//        targetState = albumLoadingState,
//        onLoadErrorContent = {
//            Box(modifier = Modifier.fillMaxSize()) {
//                Text(text = "loading")
//            }
//        }
//    ) { album ->
//        Songs(
//            modifier = Modifier.fillMaxSize(),
//            mediaIds = album.songs.map { it.mediaId },
//            sortFor = "ALBUM_DETAIL",
//            supportListAction = { emptyList() },
//            headerContent = {
//                item {
//                    AlbumCoverCard(
//                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        elevation = 2.dp,
//                        imageData = { album },
//                        onClick = { }
//                    )
//                }
//
//                item {
//                    NavigatorHeader(
//                        title = album.name,
//                        subTitle = "共 ${it.value.values.flatten().size} 首歌曲，总时长 ${
//                            album.requireItemsDuration().durationToTime()
//                        }"
//                    )
//                }
//            }
//        )
//    }
}

fun Long.durationToTime(): String {
    val hour = this / 3600000
    val minute = this / 60000 % 60
    val second = this / 1000 % 60
    return if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
    else "%02d:%02d".format(minute, second)
}