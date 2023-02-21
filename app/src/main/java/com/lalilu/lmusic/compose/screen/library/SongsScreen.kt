package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.blankj.utilcode.util.TimeUtils
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.base.SortPanel
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.SongDetailScreen
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.*
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalAnimationApi::class)
object SongsScreen : BaseScreen() {
    @OptIn(ExperimentalMaterialApi::class)
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.Songs.name}?showAll={showAll}&title={title}",
            arguments = listOf(
                navArgument("showAll") {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = "全部歌曲"
                }
            )
        ) { backStackEntry ->
            val showAll = backStackEntry.arguments?.getBoolean("showAll") ?: true
            val title = backStackEntry.arguments?.getString("title") ?: "全部歌曲"

            SongsScreen(
                showAll = showAll
            ) { songs, showSortBar ->
                item {
                    NavigatorHeader(
                        title = title, subTitle = "共 ${songs.size} 首歌曲"
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = dayNightTextColor(0.05f),
                            onClick = showSortBar
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.subtitle2,
                                color = dayNightTextColor(0.7f),
                                text = "排序"
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Songs.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.Songs.name}?showAll=$argv"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsScreen(
    showAll: Boolean = true,
    playingVM: PlayingViewModel = getViewModel(),
    songsViewModel: SongsViewModel = getViewModel(),
    sortFor: String = Sortable.SORT_FOR_SONGS,
    headerContent: LazyGridScope.(songs: List<LSong>, showSortBar: () -> Unit) -> Unit = { _, _ -> }
) {
    val navToSongAction = SongDetailScreen.navToByArgv(hapticType = HapticFeedbackType.LongPress)
    val songsState by songsViewModel.songsState

    LaunchedEffect(showAll) {
        if (showAll) {
            songsViewModel.updateByLibrary()
        }
    }

    SongsSelectWrapper { selector ->
        SmartContainer.LazyVerticalGrid(
            columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val now = System.currentTimeMillis()

            headerContent(songsState.values.flatten()) {
                SmartBar.setMainBar {
                    SortPanel(
                        supportGroupRules = { songsViewModel.supportGroupRules },
                        supportOrderRules = { songsViewModel.supportOrderRules },
                        supportSortRules = { songsViewModel.supportSortRules },
                        recoverTo = LibraryDetailNavigateBar,
                        sortFor = sortFor
                    )
                }
            }

            songsState.forEach { (titleObj, list) ->
                if (titleObj is Long) {
                    item(
                        key = titleObj,
                        contentType = LSong::dateAdded,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp
                            ), style = MaterialTheme.typography.h6, text = when {
                                now - titleObj < 300000 -> "刚刚"
                                now - titleObj < 3600000 -> "${(now - titleObj) / 60000}分钟前"
                                now - titleObj < 86400000 -> "${(now - titleObj) / 3600000}小时前"
                                else -> TimeUtils.millis2String(titleObj, "M月d日 HH:mm")
                            }
                        )
                    }
                } else if (titleObj is String && titleObj.isNotEmpty()) {
                    item(
                        key = titleObj,
                        contentType = LSong::dateAdded,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp
                            ), style = MaterialTheme.typography.h6, text = titleObj
                        )
                    }
                }

                items(
                    items = list,
                    key = { it.id },
                    contentType = { LSong::class }
                ) { item ->
                    SongCard(
                        modifier = Modifier.animateItemPlacement(),
                        song = { item },
                        lyricRepository = playingVM.lyricRepository,
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                playingVM.playSongWithPlaylist(songsState.values.flatten(), item)
                            }
                        },
                        onLongClick = { navToSongAction(item.id) },
                        onEnterSelect = { selector.onSelected(item) },
                        isSelected = { selector.selectedItems.any { it.id == item.id } }
                    )
                }
            }
        }
    }
}