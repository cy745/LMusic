package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ConvertUtils
import com.lalilu.R
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.DictionaryDetailScreenDestination
import com.lalilu.lmusic.viewmodel.DictionariesViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun DictionariesScreen(
    mediaVM: LMediaViewModel = get(),
    dictionariesVM: DictionariesViewModel = get(),
    navigator: DestinationsNavigator
) {
    val dictionaries by mediaVM.dictionaries.collectAsState(initial = emptyList())
    val blockedPaths by dictionariesVM.getBlockedPathsFlow().collectAsState(initial = emptyList())

    SmartContainer.LazyColumn {
        item {
            NavigatorHeader(title = "全部文件夹", subTitle = "长按以进行屏蔽操作")
        }

        items(items = dictionaries) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navigator.navigate(DictionaryDetailScreenDestination(dictionaryId = item.id))
                        },
                        onLongClick = {
                            if (blockedPaths.contains(item.path)) {
                                dictionariesVM.recoverPath(item.path)
                            } else {
                                dictionariesVM.blockPath(item.path)
                            }
                        }
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = blockedPaths.contains(item.path)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_eye_off_fill),
                        contentDescription = "",
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp),
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