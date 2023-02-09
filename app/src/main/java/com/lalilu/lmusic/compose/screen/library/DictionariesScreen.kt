package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.blankj.utilcode.util.ConvertUtils
import com.google.accompanist.navigation.animation.composable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.DictionariesSelectWrapper
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.DictionaryDetailScreen
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.LocalLibraryVM

@OptIn(ExperimentalAnimationApi::class)
object DictionariesScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Dictionaries.name) {
            DictionariesScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Dictionaries.name
    }
}

@Composable
fun DictionariesScreen(
    libraryVM: LibraryViewModel = LocalLibraryVM.current
) {
    val dictionaries by libraryVM.dictionaries
    val navToDictionaryAction = DictionaryDetailScreen.navToByArgv()

    DictionariesSelectWrapper { selector ->
        SmartContainer.LazyColumn {
            items(items = dictionaries) { item ->
                Row(
                    modifier = Modifier
                        .clickable {
                            when {
                                selector.isSelecting.value -> {
                                    selector.onSelected(item)
                                }

                                else -> {
                                    navToDictionaryAction(item.id)
                                }
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = item.name, style = MaterialTheme.typography.subtitle1)
                        Text(
                            text = item.path,
                            maxLines = 1,
                            style = MaterialTheme.typography.subtitle2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${item.requireItemsCount()} 首歌曲",
                            style = MaterialTheme.typography.subtitle2
                        )
                        Text(
                            text = ConvertUtils.byte2FitMemorySize(item.requireFileSize()),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
        }
    }
}