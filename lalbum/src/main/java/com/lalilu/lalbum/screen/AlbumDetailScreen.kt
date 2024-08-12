package com.lalilu.lalbum.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.Songs
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.lalbum.R
import com.lalilu.lalbum.component.AlbumCoverCard
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class AlbumDetailScreen(
    private val albumId: String
) : DynamicScreen(), ScreenType.List {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.album_screen_title,
    )

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
private fun DynamicScreen.AlbumDetail(
    albumDetailSM: AlbumDetailScreenModel
) {
    val albumLoadingState = albumDetailSM.album.collectAsLoadingState()

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = albumLoadingState,
        onLoadErrorContent = {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(text = "loading")
            }
        }
    ) { album ->
        Songs(
            modifier = Modifier.fillMaxSize(),
            mediaIds = album.songs.map { it.mediaId },
            sortFor = "ALBUM_DETAIL",
            supportListAction = { emptyList() },
            headerContent = {
                item {
                    AlbumCoverCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = 2.dp,
                        imageData = { album },
                        onClick = { }
                    )
                }

                item {
                    NavigatorHeader(
                        title = album.name,
                        subTitle = "共 ${it.value.values.flatten().size} 首歌曲，总时长 ${
                            album.requireItemsDuration().durationToTime()
                        }"
                    )
                }
            }
        )
    }
}

fun Long.durationToTime(): String {
    val hour = this / 3600000
    val minute = this / 60000 % 60
    val second = this / 1000 % 60
    return if (hour > 0L) "%02d:%02d:%02d".format(hour, minute, second)
    else "%02d:%02d".format(minute, second)
}