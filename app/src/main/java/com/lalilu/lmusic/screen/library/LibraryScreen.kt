package com.lalilu.lmusic.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.PaletteTransformation
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val navController = LocalNavigatorHost.current
    val dailyRecommends = remember { viewModel.requireDailyRecommends() }
    val lastPlayedStack by viewModel.requirePlayHistory().collectAsState(emptyList())
    val randomRecommends = remember { Library.getSongs(15, true) }
    val currentPlaying by LMusicRuntime.currentPlayingFlow.collectAsState()
    val currentIsPlaying by LMusicRuntime.currentIsPlayingFlow.collectAsState()

    val playSong = remember {
        { mediaId: String ->
            currentPlaying.takeIf { it != null && it.id == mediaId && currentIsPlaying }
                ?.let { LMusicBrowser.pause() }
                ?: LMusicBrowser.addAndPlay(mediaId)
        }
    }

    val showDetail = remember {
        { mediaId: String ->
            navController.navigate("${MainScreenData.SongsDetail.name}/$mediaId") {
                launchSingleTop = true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = SmartBar.rememberContentPadding()
    ) {
        dailyRecommends.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("每日推荐", onClick = { })
            }
            item {
                RecommendRow(
                    items = it
                ) {
                    RecommendCard(
                        song = it,
                        width = 250.dp,
                        height = 250.dp,
                        onShowDetail = showDetail
                    )
                }
            }
        }

        item {
            // 最近添加
            RecommendTitle("最近添加", onClick = { })
        }
        item {
            RecommendRow(
                items = Library.getSongs(15)
            ) {
                RecommendCardWithOutSideText(
                    song = it,
                    isPlaying = (currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id),
                    onPlaySong = playSong,
                    onShowDetail = showDetail
                )
            }
        }

        lastPlayedStack.takeIf { it.isNotEmpty() }?.let {
            item {
                RecommendTitle("最近播放")
            }
            item {
                RecommendRow(
                    items = it
                ) {
                    RecommendCardWithOutSideText(
                        song = it,
                        width = 125.dp,
                        height = 125.dp,
                        isPlaying = (currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id),
                        onPlaySong = playSong,
                        onShowDetail = showDetail
                    )
                }
            }
        }

        item {
            RecommendTitle("随机推荐", onClick = { })
        }
        item {
            RecommendRow(
                items = randomRecommends
            ) {
                RecommendCard(
                    song = it,
                    width = 125.dp,
                    height = 250.dp,
                    onShowDetail = showDetail
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
        modifier = Modifier.animateContentSize(
            animationSpec = SpringSpec(stiffness = Spring.StiffnessLow)
        ),
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
fun RecommendCard(
    song: LSong,
    width: Dp = 200.dp,
    height: Dp = 125.dp,
    onShowDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var cardMainColor by remember { mutableStateOf(Color.Gray) }
    val gradientStartOffsetY = remember(density) { density.run { height.toPx() } / 2f }
    val transformation = remember {
        PaletteTransformation {
            cardMainColor = Color(it.getDarkVibrantColor(android.graphics.Color.GRAY))
        }
    }

    Surface(
        elevation = 1.dp,
        color = Color.LightGray,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clickable { onShowDetail(song.id) }
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(context)
                    .transformations(transformation)
                    .data(song)
                    .build(),
                contentDescription = ""
            )
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                cardMainColor
                            ),
                            start = Offset(0f, gradientStartOffsetY),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .fillMaxSize()
                    .padding(20.dp)
                    .align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom)
            ) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )
                Text(
                    text = song._artist,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.White.copy(0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RecommendCardWithOutSideText(
    song: LSong,
    width: Dp = 200.dp,
    height: Dp = 125.dp,
    isPlaying: Boolean = false,
    onShowDetail: (String) -> Unit = {},
    onPlaySong: (String) -> Unit = {}
) {
    var showFullInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        com.lalilu.lmusic.screen.component.card.RecommendCard(
            data = { song },
            getId = { song.id },
            isPlaying = isPlaying,
            width = width,
            height = height,
            onPlay = onPlaySong,
            onShowDetail = onShowDetail
        )
        AnimatedContent(targetState = showFullInfo) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(MutableInteractionSource(), indication = null) {
                        showFullInfo = !it
                    }
            ) {
                Text(
                    maxLines = if (it) Int.MAX_VALUE else 1,
                    softWrap = it,
                    overflow = if (it) TextOverflow.Visible else TextOverflow.Ellipsis,
                    text = song.name, style = MaterialTheme.typography.subtitle1
                )
                Text(
                    modifier = Modifier.alpha(0.5f),
                    maxLines = if (it) Int.MAX_VALUE else 1,
                    softWrap = it,
                    overflow = if (it) TextOverflow.Visible else TextOverflow.Ellipsis,
                    text = song._artist,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}