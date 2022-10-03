package com.lalilu.lmusic.screen.library.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.hilt.navigation.compose.hiltViewModel
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmusic.screen.ScreenActions
import com.lalilu.lmusic.screen.bean.SORT_BY_TIME
import com.lalilu.lmusic.screen.bean.next
import com.lalilu.lmusic.screen.component.NavigatorHeaderWithButtons
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.screen.component.button.LazyListSortToggleButton
import com.lalilu.lmusic.screen.component.button.SortToggleButton
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistDetailScreen(
    artist: LArtist,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val songs = artist.songs
    val windowSize = LocalWindowSize.current
    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)
    var sortByState by rememberDataSaverState("KEY_SORT_BY_ArtistDetailScreen", SORT_BY_TIME)
    var sortDesc by rememberDataSaverState("KEY_SORT_DESC_ArtistDetailScreen", true)

    val onSongSelected: (Int) -> Unit = remember {
        { index: Int ->
            mainViewModel.playSongWithPlaylist(
                items = songs.toMutableList(),
                index = index
            )
        }
    }

    SmartContainer.LazyVerticalGrid(
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
    ) {
        item {
            NavigatorHeaderWithButtons(
                title = artist.name,
                subTitle = "${artist.getSongCount()} 首歌曲"
            ) {
                LazyListSortToggleButton(sortByState = sortByState) {
                    sortByState = next(sortByState)
                }
                SortToggleButton(sortDesc = sortDesc) {
                    sortDesc = !sortDesc
                }
            }
        }

        itemsIndexed(items = songs) { index, item ->
            SongCard(
                modifier = Modifier.animateItemPlacement(),
                index = index,
                getSong = { item },
                onSongSelected = onSongSelected,
                onSongShowDetail = navToSongAction
            )
        }
    }
}

@Composable
fun EmptyArtistDetailScreen() {

}
