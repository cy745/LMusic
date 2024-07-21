package com.lalilu.lartist.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.koin.getScreenModel
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LoadingScaffold
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.base.collectAsLoadingState
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lartist.R
import com.lalilu.lartist.component.ArtistCard
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.lalilu.component.R as ComponentR

data class ArtistsScreen(
    val artistsName: List<String> = emptyList()
) : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.artist_screen_title,
        icon = ComponentR.drawable.ic_user_line
    )

    @Composable
    override fun Content() {
        val artistsSM = getScreenModel<ArtistsScreenModel>()

        LaunchedEffect(Unit) {
            artistsSM.updateArtistsName(artistsName)
        }

        ArtistsScreen(artistsSM = artistsSM)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistsScreenModel : ScreenModel {
    private val artistsName = MutableStateFlow<List<String>>(emptyList())
    val artists = artistsName.flatMapLatest {
        if (it.isEmpty()) LMedia.getFlow<LArtist>()
        else LMedia.flowMapBy<LArtist>(it)
    }

    fun updateArtistsName(artistsName: List<String>) = screenModelScope.launch {
        this@ArtistsScreenModel.artistsName.emit(artistsName)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DynamicScreen.ArtistsScreen(
    artistsSM: ArtistsScreenModel,
    playingVM: IPlayingViewModel = koinInject()
) {
    val artistsState = artistsSM.artists.collectAsLoadingState()

    LoadingScaffold(
        modifier = Modifier.fillMaxSize(),
        targetState = artistsState
    ) { artists ->
        LLazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                NavigatorHeader(
                    modifier = Modifier.statusBarsPadding(),
                    title = stringResource(id = R.string.artist_screen_title),
                    subTitle = stringResource(id = R.string.artist_screen_title)
                )
            }

            itemsIndexed(
                items = artists,
                key = { _, item -> item.id },
                contentType = { _, _ -> LArtist::class }
            ) { index, item ->
                ArtistCard(
                    modifier = Modifier.animateItemPlacement(),
                    title = item.name,
                    subTitle = "#$index",
                    songCount = item.requireItemsCount(),
                    imageSource = { item.songs.firstOrNull()?.imageSource },
                    isPlaying = {
                        playingVM.isItemPlaying { playing ->
                            playing.let { it as? LSong }
                                ?.let { song -> song.artists.any { it.name == item.name } }
                                ?: false
                        }
                    },
                    onClick = {
                        AppRouter.intent(
                            NavIntent.Push(ArtistDetailScreen(item.id))
                        )
                    }
                )
            }
        }
    }
}