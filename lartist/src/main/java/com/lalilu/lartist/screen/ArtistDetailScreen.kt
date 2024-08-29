package com.lalilu.lartist.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.lartist.R
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LArtist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class ArtistDetailScreen(
    private val artistName: String
) : DynamicScreen() {
    override val key: ScreenKey = "ARTIST_DETAIL_$artistName"

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.artist_screen_detail,
    )

    @Composable
    override fun Content() {
        val artistDetailSM: ArtistDetailScreenModel = getScreenModel()

        LaunchedEffect(Unit) {
            artistDetailSM.updateArtistName(artistName)
        }

        ArtistDetail(artistDetailSM = artistDetailSM)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailScreenModel : ScreenModel {
    private val artistName = MutableStateFlow<String?>(null)
    val artist = artistName.flatMapLatest { LMedia.getFlow<LArtist>(it) }

    fun updateArtistName(artistName: String) = screenModelScope.launch {
        this@ArtistDetailScreenModel.artistName.emit(artistName)
    }
}

@Composable
private fun DynamicScreen.ArtistDetail(
    artistDetailSM: ArtistDetailScreenModel
) {
    val artistState = artistDetailSM.artist.collectAsLoadingState()

//    LoadingScaffold(targetState = artistState) { artist ->
//        val relateArtist = remember {
//            derivedStateOf {
//                artist.songs.map { it.artists }
//                    .flatten()
//                    .toSet()
//                    .filter { it.id != artist.name }
//            }
//        }
//
//        Songs(
//            mediaIds = artist.songs.map { it.mediaId },
//            selectActions = { getAll ->
//                listOf(SelectAction.StaticAction.SelectAll(getAll))
//            },
//            sortFor = "ArtistDetail",
//            supportListAction = { emptyList() },
//            headerContent = {
//                item {
//                    NavigatorHeader(
//                        title = artist.name,
//                        subTitle = "共 ${artist.requireItemsCount()} 首歌曲，总时长 ${
//                            artist.requireItemsDuration().durationToTime()
//                        }"
//                    )
//                }
//            },
//            footerContent = {
//                if (relateArtist.value.isNotEmpty()) {
//                    item {
//                        NavigatorHeader(
//                            modifier = Modifier.padding(top = 20.dp),
//                            titleScale = 0.8f,
//                            title = "相关歌手",
//                            subTitle = "共 ${relateArtist.value.size} 位"
//                        )
//                    }
//                    items(items = relateArtist.value) {
//                        ArtistCard(
//                            artist = it,
//                            onClick = {
//                                AppRouter.intent(
//                                    NavIntent.Push(ArtistDetailScreen(it.id))
//                                )
//                            }
//                        )
//                    }
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