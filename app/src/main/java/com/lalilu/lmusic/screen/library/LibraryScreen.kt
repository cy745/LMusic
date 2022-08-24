package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.utils.FadeEdgeTransformation

@Composable
fun LibraryScreen() {
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = contentPaddingForFooter)
    ) {
        item {
            RecommendTitle("每日推荐set", onClick = {

            })
        }
        item {
            RecommendRow(
                items = Library.getSongs(10, random = true)
            ) {

                RecommendCard(
                    song = it,
                    width = 250.dp,
                    height = 250.dp
                )
            }
        }

        item {
            // 最近添加
            RecommendTitle("最近添加add toggle", onClick = {
                SmartBar.setExtraBar(toggle = true) {

                }
            })
        }
        item {
            RecommendRow(
                items = Library.getSongs(10)
            ) {
                RecommendCard(song = it)
            }
        }

        item {
            RecommendTitle("最近播放")
        }
        item {
            RecommendRow(
                items = Library.getSongs(10)
            ) {
                RecommendCard(
                    song = it,
                    width = 125.dp,
                    height = 125.dp
                )
            }
        }

        item {
            RecommendTitle("每日推荐add", onClick = {

            })
        }
        item {
            RecommendRow(
                items = Library.getSongs(10)
            ) {
                RecommendCard(
                    song = it,
                    width = 250.dp,
                    height = 250.dp
                )
            }
        }
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
fun <I> RecommendRow(
    items: Collection<I>,
    itemContent: @Composable LazyItemScope.(item: I) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items.forEach {
            item { itemContent(it) }
        }
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
fun RecommendCard(song: LSong, width: Dp = 200.dp, height: Dp = 125.dp) {
    val context = LocalContext.current
    Surface(
        elevation = 1.dp,
        color = Color.LightGray,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(context)
                    .data(song)
                    .transformations(FadeEdgeTransformation())
                    .build(),
                contentDescription = ""
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = song.name, style = MaterialTheme.typography.subtitle1)
                Text(text = song._artist, style = MaterialTheme.typography.subtitle2)
            }
        }
    }
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