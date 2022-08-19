package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.screen.component.SmartBar

@Composable
fun LibraryScreen(
    contentPaddingForFooter: Dp = 0.dp
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = contentPaddingForFooter)
    ) {
        item {
            RecommendTitle("每日推荐set", onClick = {
                SmartBar.setBarItem(toggle = true) {
                    Text(
                        text = "每日推荐set", modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }
            })
        }
        item {
            RecommendRow(
                items = Indexer.library.songs
                    .sortedByDescending { it.id.toLongOrNull() ?: 0 }
                    .take(10)
            ) {
                RecommendCard(
                    title = it.name,
                    subTitle = it._artist,
                    width = 250.dp,
                    height = 250.dp
                )
            }
        }

        item {
            // 最近添加
            RecommendTitle("最近添加add toggle", onClick = {
                SmartBar.addBarItem(toggle = true) {
                    Text(
                        text = "最近添加add toggle", modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }
            })
        }
        item {
            RecommendRow(
                items = Indexer.library.songs
                    .sortedByDescending { it.id.toLongOrNull() ?: 0 }
                    .take(10)
            ) {
                RecommendCard(
                    title = it.name,
                    subTitle = it._artist
                )
            }
        }

        item {
            RecommendTitle("最近播放")
        }
        item {
            RecommendRow(
                items = Indexer.library.songs
                    .sortedByDescending { it.id.toLongOrNull() ?: 0 }
                    .take(10)
            ) {
                RecommendCard(
                    title = it.name,
                    subTitle = it._artist,
                    width = 125.dp,
                    height = 125.dp
                )
            }
        }

        item {
            RecommendTitle("每日推荐add", onClick = {
                SmartBar.addBarItem {
                    Text(
                        text = "每日推荐add", modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }
            })
        }
        item {
            RecommendRow(
                items = Indexer.library.songs
                    .sortedByDescending { it.id.toLongOrNull() ?: 0 }
                    .take(10)
            ) {
                RecommendCard(
                    title = it.name,
                    subTitle = it._artist,
                    width = 250.dp,
                    height = 250.dp
                )
            }
        }


//        LazyVerticalGrid(
//            columns = GridCells.Fixed(if (currentWindowSize == WindowSize.Expanded) 3 else 2),
//            contentPadding = PaddingValues(20.dp),
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(20.dp),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            MainScreenData.values().forEach {
//                if (it.showNavigateButton) {
//                    item {
//                        DestinationCard(
//                            route = it,
//                            navigateTo = navigateTo
//                        )
//                    }
//                }
//            }
//        }
    }
}


@Composable
fun RecommendTitle(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .weight(1f),
            text = title,
            style = MaterialTheme.typography.h6
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            contentDescription = ""
        )
    }
}

@Composable
fun <I> RecommendRow(items: List<I>, itemContent: @Composable LazyItemScope.(item: I) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(items = items, itemContent = itemContent)
    }
}

@Composable
fun RecommendRow(content: LazyListScope.() -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        content = content,
    )
}

@Composable
fun RecommendCard(title: String, subTitle: String, width: Dp = 200.dp, height: Dp = 125.dp) {
    Surface(
        elevation = 1.dp,
        color = Color.LightGray,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .width(width)
                .height(height)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
            Text(text = subTitle, style = MaterialTheme.typography.subtitle2)
        }
    }
}