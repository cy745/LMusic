package com.lalilu.lmusic.compose.screen.library.detail

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.SongsScreen
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.SongsViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalAnimationApi::class)
object DictionaryDetailScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(
            route = "${ScreenData.DictionaryDetail.name}/{dictionaryId}",
            arguments = listOf(navArgument("dictionaryId") {})
        ) { backStackEntry ->
            val dictionaryId = backStackEntry.arguments?.getString("dictionaryId")

            LMedia.getDictionaryOrNull(id = dictionaryId, blockFilter = false)
                ?.let { DictionaryDetailScreen(dictionary = it) }
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.DictionaryDetail.name
    }

    override fun getNavToByArgvRoute(argv: String): String {
        return "${ScreenData.DictionaryDetail.name}/$argv"
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DictionaryDetailScreen(
    dictionary: LDictionary,
    songsVM: SongsViewModel = get()
) {
    val sortFor = remember { "DictionaryDetail" }

    LaunchedEffect(dictionary) {
        songsVM.updateBySongs(
            songs = dictionary.songs,
            sortFor = sortFor
        )
    }

    SongsScreen(
        showAll = false,
        sortFor = sortFor
    ) { songs, showSortBar ->
        item {
            NavigatorHeader(
                title = dictionary.name,
                subTitle = dictionary.path.let { "$it\n共 ${songs.size} 首歌曲" }
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