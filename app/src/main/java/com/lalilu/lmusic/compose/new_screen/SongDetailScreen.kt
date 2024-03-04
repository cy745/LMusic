package com.lalilu.lmusic.compose.new_screen

import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.ScreenKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.component.IconButton
import com.lalilu.component.IconTextButton
import com.lalilu.component.LLazyColumn
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.extension.DynamicTipsItem
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.rememberScrollPosition
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lalbum.screen.AlbumDetailScreen
import com.lalilu.lartist.screen.ArtistDetailScreen
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.base.IconCheckButton
import com.lalilu.lmusic.compose.component.card.RecommendCardCover
import com.lalilu.lmusic.compose.component.card.SongInformationCard
import com.lalilu.lmusic.compose.presenter.DetailScreenAction
import com.lalilu.lmusic.compose.presenter.DetailScreenIsPlayingPresenter
import com.lalilu.lmusic.compose.presenter.DetailScreenLikeBtnPresenter
import com.lalilu.lmusic.utils.extension.EDGE_BOTTOM
import com.lalilu.lmusic.utils.extension.checkActivityIsExist
import com.lalilu.lmusic.utils.extension.edgeTransparent
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lplayer.extensions.QueueAction
import org.koin.compose.koinInject

data class SongDetailScreen(
    private val mediaId: String
) : DynamicScreen() {
    override val key: ScreenKey = "${super.key}:$mediaId"

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_song_detail
    )

    @Composable
    override fun registerActions(): List<ScreenAction> {
        return remember {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.button_set_song_to_next,
                    color = Color(0xFF00AC84),
                    onAction = {
                        val song = LMedia.get<LSong>(id = mediaId) ?: return@StaticAction
                        QueueAction.AddToNext(song.mediaId).action()
                        DynamicTipsItem.Static(
                            title = song.title,
                            subTitle = "下一首播放",
                            imageData = song.imageSource
                        ).show()
                    }
                ),
                ScreenAction.ComposeAction {
                    val state = DetailScreenLikeBtnPresenter(mediaId)

                    IconCheckButton(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(4f / 3f),
                        shape = RectangleShape,
                        getIsChecked = { state.isLiked },
                        onCheckedChange = { state.onAction(if (it) DetailScreenAction.Like else DetailScreenAction.UnLike) },
                        checkedColor = MaterialTheme.colors.primary,
                        checkedIconRes = R.drawable.ic_heart_3_fill,
                        normalIconRes = R.drawable.ic_heart_3_line
                    )
                },
                ScreenAction.ComposeAction {
                    val state = DetailScreenIsPlayingPresenter(mediaId)

                    AnimatedContent(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(3f / 2f),
                        targetState = state.isPlaying,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = ""
                    ) { isPlaying ->
                        val icon =
                            if (isPlaying) R.drawable.ic_pause_line else R.drawable.ic_play_line
                        IconButton(
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF006E7C),
                            shape = RectangleShape,
                            text = stringResource(id = R.string.text_button_play),
                            icon = painterResource(id = icon),
                            onClick = { state.onAction(DetailScreenAction.PlayPause) }
                        )
                    }
                },
            )
        }
    }

    @Composable
    override fun Content() {
        val song = LMedia.getFlow<LSong>(id = mediaId)
            .collectAsState(initial = null)

        DetailScreen(
            mediaId = { mediaId },
            getSong = { song.value }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DetailScreen(
    mediaId: () -> String,
    getSong: () -> LSong?
) {
    val navigator: GlobalNavigator = koinInject()
    val context = LocalContext.current
    val song = getSong()

    if (song == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "[Error]加载失败 #${mediaId()}")
        }
        return
    }

    val listState = rememberLazyListState()
    val scrollPosition = rememberScrollPosition(state = listState)
    val bgAlpha = remember {
        derivedStateOf {
            return@derivedStateOf 1f - (scrollPosition.value / 500f)
                .coerceIn(0f, 0.8f)
        }
    }

    val intent = remember(song) {
        Intent().apply {
            component = ComponentName(
                "com.xjcheng.musictageditor",
                "com.xjcheng.musictageditor.SongDetailActivity"
            )
            action = "android.intent.action.VIEW"
            data = song.uri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .recomposeHighlighter(),
        contentAlignment = Alignment.TopCenter
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .edgeTransparent(position = EDGE_BOTTOM, percent = 1.5f)
                .graphicsLayer { alpha = bgAlpha.value },
            model = ImageRequest.Builder(context)
                .data(song)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = ""
        )

        LLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .recomposeHighlighter(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(150.dp))
            }

            item {
                NavigatorHeader(
                    title = song.name,
                    columnExtraContent = {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            song.artists.forEach {
                                Chip(
                                    onClick = { navigator.navigateTo(ArtistDetailScreen(artistName = it.name)) },
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
                )
            }

            song.album?.let {
                item {
                    Surface(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { navigator.navigateTo(AlbumDetailScreen(albumId = it.id)) }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (context.checkActivityIsExist(intent)) {
                        IconTextButton(
                            text = "音乐标签编辑",
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF3EA22C),
                            onClick = {
                                if (context.checkActivityIsExist(intent)) {
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "未安装[音乐标签]", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        )
                    }
                    IconTextButton(
                        text = "搜索LrcShare",
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF3EA22C),
                        onClick = {
                            navigator.navigateTo(
                                SearchLyricScreen(
                                    mediaId = song.id,
                                    keywords = song.name
                                )
                            )
                        }
                    )
                }
            }

            item {
                SongInformationCard(
                    modifier = Modifier.fillMaxWidth(),
                    song = song
                )
            }
        }
    }
}