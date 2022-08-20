package com.lalilu.lmusic.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.R
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.navigate
import com.lalilu.lmusic.utils.LocalNavigatorHost

@Composable
fun LibraryScreen(
    contentPaddingForFooter: Dp = 0.dp
) {
    LaunchedEffect(Unit) {
        SmartBar.setBarItem {
            NavigateBar()
        }
    }
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
    }
}

@Composable
fun NavigateBar() {
    val navController = LocalNavigatorHost.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val isLibraryScreen = currentRoute?.contains(MainScreenData.Library.name) ?: false

    LazyRow(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MainScreenData.values().forEach {
            if (it.showNavigateButton) {
                item {
                    NavigateItem(
                        navController = navController,
                        currentRoute = currentRoute,
                        routeData = it
                    )
                }
            }
        }
    }
}

@Composable
fun NavigateItem(
    navController: NavController,
    routeData: MainScreenData,
    currentRoute: String?,
    baseColor: Color = MaterialTheme.colors.primary,
) {
    val icon = painterResource(id = routeData.icon)
    val title = stringResource(id = routeData.title)
    val selected = currentRoute?.contains(routeData.name) ?: false
    val imageAlpha = if (selected) 1f else 0.6f
    val iconTintColor = if (selected) baseColor else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    val backgroundColor by animateColorAsState(
        if (selected) baseColor.copy(alpha = 0.12f) else Color.Transparent
    )

    @OptIn(ExperimentalMaterialApi::class)
    Surface(
        color = backgroundColor,
        onClick = {
            navController.navigate(
                from = MainScreenData.Library.name,
                to = routeData.name
            )
        },
        shape = CircleShape,
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = icon,
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(iconTintColor),
                    alpha = imageAlpha,
                    contentScale = FixedScale(if (selected) 1.1f else 1f)
                )
                AnimatedVisibility(visible = selected) {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.1.sp
                    )
                }
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