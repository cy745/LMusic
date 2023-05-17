package com.lalilu.lmusic.compose.new_screen

import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowRow
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.RecommendCardCover
import com.lalilu.lmusic.compose.component.card.SongDetailActionsBar
import com.lalilu.lmusic.compose.component.card.SongInformationCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistsScreenDestination
import com.lalilu.lmusic.utils.extension.EDGE_BOTTOM
import com.lalilu.lmusic.utils.extension.checkActivityIsExist
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.edgeTransparent
import com.lalilu.lmusic.utils.extension.idsText
import com.lalilu.lmusic.utils.extension.rememberScrollPosition
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.lalilu.lmusic.viewmodel.SongDetailViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun SongDetailScreen(
    mediaId: String,
    playingVM: PlayingViewModel = get(),
    playlistsVM: PlaylistsViewModel = get(),
    songDetailVM: SongDetailViewModel = get(),
    navigator: DestinationsNavigator
) {
    LaunchedEffect(Unit) {
        songDetailVM.updateMediaId(mediaId)
    }

    val context = LocalContext.current
    val song = songDetailVM.song.collectAsState(initial = null).value
    if (song == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$mediaId")
        }
        return
    }

    val showActionBar = remember { mutableStateOf(true) }
    val isLiked by playlistsVM.checkIsFavorite(song).collectAsState(initial = false)
    val gridState = rememberLazyGridState()
    val scrollPosition = rememberScrollPosition(state = gridState)
    val bgAlpha = remember {
        derivedStateOf {
            return@derivedStateOf 1f - (scrollPosition.value / 500f)
                .coerceIn(0f, 0.8f)
        }
    }

    SmartModalBottomSheet.RegisterForTemporaryDisableFadeEdge()
    SmartBar.RegisterExtraBarContent(showActionBar) {
        SongDetailActionsBar(
            isPlaying = { playingVM.isSongPlaying(song.id) },
            getIsLiked = { isLiked },
            onIsLikedChange = {
                if (it) playlistsVM.addToFavorite(song) else playlistsVM.removeFromFavorite(song)
            },
            onAddSongToPlaylist = {
                navigator.navigate(PlaylistsScreenDestination(idsText = listOf(song).idsText()))
            },
            onSetSongToNext = {
                DynamicTips.push(
                    title = song.name,
                    subTitle = "下一首播放",
                    imageData = song
                )
                playingVM.browser.addToNext(song.id)
            },
            onPlayOrPause = {
                playingVM.play(
                    mediaId = song.id,
                    addToNext = true,
                    playOrPause = true
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .recomposeHighlighter(),
        contentAlignment = Alignment.TopCenter
    ) {
        AsyncImage(
            modifier = Modifier
                .recomposeHighlighter()
                .fillMaxWidth()
                .edgeTransparent(position = EDGE_BOTTOM, percent = 1.5f)
                .graphicsLayer {
                    alpha = bgAlpha.value
                },
            model = ImageRequest.Builder(LocalContext.current)
                .data(song)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = ""
        )
        SmartContainer.LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 }
        ) {
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }

            item {
                NavigatorHeader(
                    title = song.name,
                    columnExtraContent = {
                        FlowRow(mainAxisSpacing = 8.dp) {
                            song.artists.forEach {
                                Chip(
                                    onClick = {
                                        navigator.navigate(
                                            ArtistDetailScreenDestination(artistName = it.name)
                                        )
                                    },
                                    colors = ChipDefaults.outlinedChipColors(),
                                ) {
                                    Text(
                                        text = it.name,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                                            .copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                ) {
//                    IconButton(onClick = { }) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_fullscreen_line),
//                            contentDescription = "查看图片"
//                        )
//                    }
                }
            }

            song.album?.let {
                item {
                    Surface(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                            .width(intrinsicSize = IntrinsicSize.Min),
                        shape = RoundedCornerShape(20.dp),
                        onClick = {
                            navigator.navigate(AlbumDetailScreenDestination(it.id))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecommendCardCover(
                                width = { 125.dp },
                                height = { 125.dp },
                                imageData = { it }
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.subtitle1,
                                    color = dayNightTextColor()
                                )
                                it.artistName?.let { it1 ->
                                    Text(
                                        text = it1,
                                        style = MaterialTheme.typography.subtitle2,
                                        color = dayNightTextColor(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                IconTextButton(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    text = "使用音乐标签编辑信息",
                    color = Color(0xFF3EA22C),
                    onClick = {
                        val intent = Intent().apply {
                            component = ComponentName(
                                "com.xjcheng.musictageditor",
                                "com.xjcheng.musictageditor.SongDetailActivity"
                            )
                            action = "android.intent.action.VIEW"
                            data = song.uri
                        }
                        if (context.checkActivityIsExist(intent)) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "未安装[音乐标签]", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }

            item {
                SongInformationCard(song = song)
            }
        }
    }
}