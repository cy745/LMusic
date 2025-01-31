package com.lalilu.lmusic.compose.screen.playing

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.component.base.LocalEnhanceSheetState
import com.lalilu.component.extension.DynamicTipsItem
import com.lalilu.component.extension.hideControl
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.LyricSourceEmbedded
import com.lalilu.lmedia.lyric.LyricUtils
import com.lalilu.lmusic.compose.component.playing.LyricViewToolbar
import com.lalilu.lmusic.compose.component.playing.PlayingToolbar
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricLayout
import com.lalilu.lmusic.compose.screen.playing.lyric.index
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.rememberFontFamilyFromPath
import com.lalilu.lmusic.compose.screen.playing.seekbar.ClickPart
import com.lalilu.lmusic.compose.screen.playing.seekbar.SeekbarLayout
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplayer.extensions.PlayMode
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.pow

@Composable
fun PlayingLayout(
    settingsSp: SettingsSp = koinInject(),
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycle = LocalLifecycleOwner.current
    val enhanceSheetState = LocalEnhanceSheetState.current
    val systemUiController = rememberSystemUiController()
    val listState = rememberLazyListState()

    val isLyricScrollEnable = remember { mutableStateOf(false) }
    val backgroundColor = remember { mutableStateOf(Color.DarkGray) }
    val animateColor = animateColorAsState(targetValue = backgroundColor.value, label = "")
    val scrollToTopEvent = remember { mutableStateOf(0L) }
    val seekbarTime = remember { mutableLongStateOf(0L) }
    val currentPosition = remember { mutableFloatStateOf(0f) }

    val draggable = rememberCustomAnchoredDraggableState { oldState, newState ->
        if (newState == DragAnchor.MiddleXMax && oldState != DragAnchor.MiddleXMax) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (newState != DragAnchor.Max) {
            isLyricScrollEnable.value = false
        }
    }

    val hideComponent = remember {
        derivedStateOf {
            settingsSp.autoHideSeekbar.value && draggable.state.value == DragAnchor.Max
        }
    }

    LaunchedEffect(hideComponent.value) {
        systemUiController.isStatusBarVisible = !hideComponent.value
    }

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                withFrameMillis {
                    val newValue = MPlayer.currentPosition.toFloat()
                    if (currentPosition.floatValue != newValue) {
                        currentPosition.floatValue = newValue
                    }
                }
            }
        }
    }

    NestedScrollBaseLayout(
        draggable = draggable,
        isLyricScrollEnable = isLyricScrollEnable,
        toolbarContent = {
            val density = LocalDensity.current
            val navigationBar = WindowInsets.navigationBars
            val middleToMaxProgress = remember {
                derivedStateOf {
                    draggable.progressBetween(
                        from = DragAnchor.Middle,
                        to = DragAnchor.Max,
                        offset = draggable.position.floatValue
                    )
                }
            }

            Column(
                modifier = Modifier
                    .hideControl(
                        enable = { hideComponent.value },
                        intercept = { true }
                    )
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 10.dp)
                    .graphicsLayer {
                        translationY = lerp(
                            start = 0f,
                            stop = -navigationBar
                                .getBottom(density)
                                .toFloat() + 10.dp.toPx(),
                            fraction = middleToMaxProgress.value
                        )
                    }
            ) {
                PlayingToolbar(
                    isItemPlaying = { mediaId -> MPlayer.isItemPlaying(mediaId) },
                    isUserTouchEnable = { draggable.state.value == DragAnchor.Min || draggable.state.value == DragAnchor.Max },
                    isExtraVisible = { draggable.state.value == DragAnchor.Max },
                    onClick = { scrollToTopEvent.value = System.currentTimeMillis() },
                    extraContent = { LyricViewToolbar() }
                )
            }
        },
        dynamicHeaderContent = { modifier ->
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .background(color = animateColor.value)
            ) {
                val adInterpolator = remember { AccelerateDecelerateInterpolator() }
                val dInterpolator = remember { DecelerateInterpolator() }
                val transition: (Float) -> Float = remember {
                    { x -> -2f * (x - 0.5f).pow(2) + 0.5f }
                }

                val minToMiddleProgress = remember {
                    derivedStateOf {
                        draggable.progressBetween(
                            from = DragAnchor.Min,
                            to = DragAnchor.Middle,
                            offset = draggable.position.floatValue
                        )
                    }
                }
                val middleToMaxProgress = remember {
                    derivedStateOf {
                        draggable.progressBetween(
                            from = DragAnchor.Middle,
                            to = DragAnchor.Max,
                            offset = draggable.position.floatValue
                        )
                    }
                }

                BlurBackground(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            val maxHeight = constraints.maxHeight
                            val maxWidth = constraints.maxWidth

                            // min至middle阶段中的位移
                            val minToMiddleInterpolated =
                                dInterpolator.getInterpolation(minToMiddleProgress.value)
                            val minToMiddleOffset =
                                lerp(-size.width / 2f, 0f, minToMiddleInterpolated)

                            // middle至max阶段中的位移
                            val middleToMaxInterpolated =
                                dInterpolator.getInterpolation(middleToMaxProgress.value)
                            val middleToMaxOffset =
                                lerp(0f, (maxHeight - maxWidth) / 2f, middleToMaxInterpolated)

                            // 用于补偿修正因layout时根据draggable的值进行布局的位移
                            val fixOffset = maxHeight - draggable.position.floatValue

                            // 添加凸显滑动时的动画的位移
                            val progressTransited = transition(middleToMaxProgress.value)
                            val additionalOffset = progressTransited * 200f

                            // 计算父级容器的长宽比，计算需要覆盖父级容器的的缩放比例的值scale
                            val aspectRatio = maxHeight.toFloat() / maxWidth.toFloat()
                            val scale = lerp(1f, aspectRatio, middleToMaxProgress.value)

                            translationY =
                                minToMiddleOffset + middleToMaxOffset + fixOffset + additionalOffset
                            alpha = minToMiddleProgress.value
                            scaleY = scale
                            scaleX = scale
                        },
                    blurProgress = { middleToMaxProgress.value },
                    onBackgroundColorFetched = { backgroundColor.value = it },
                    imageData = {
                        MPlayer.currentMediaItem
                            ?: com.lalilu.component.R.drawable.ic_music_2_line_100dp
                    }
                )

                val lyricSource = remember { LyricSourceEmbedded(context = context) }
                val lyrics = remember { mutableStateOf<List<LyricItem>>(emptyList()) }

                LaunchedEffect(key1 = MPlayer.currentMediaItem) {
                    launch(Dispatchers.IO) {
                        MPlayer.currentMediaItem
                            ?.let { lyricSource.loadLyric(it) }
                            ?.let { LyricUtils.parseLrc(it.first, it.second) }
                            ?.mapIndexed { index, lyricItem -> lyricItem.also { it.index = index } }
                            .let { if (isActive) lyrics.value = it ?: emptyList() }
                    }
                }

                LyricLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val interpolation =
                                adInterpolator.getInterpolation(middleToMaxProgress.value)
                            val progressIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)

                            val fixOffset = size.height - draggable.position.floatValue

                            val progressTransited = transition(middleToMaxProgress.value)
                            val additionalOffset = progressTransited * 200f * 3f

                            translationY = additionalOffset + fixOffset
                            alpha = progressIncrease
                        },
                    lyricEntry = lyrics,
                    listState = listState,
                    currentTime = { seekbarTime.longValue },
                    screenConstraints = constraints,
                    fontFamily = rememberFontFamilyFromPath { settingsSp.lyricTypefacePath.value },
                    isUserClickEnable = { draggable.state.value == DragAnchor.Max },
                    isUserScrollEnable = { isLyricScrollEnable.value },
                    onPositionReset = {
                        if (isLyricScrollEnable.value) {
                            isLyricScrollEnable.value = false
                        }
                    },
                    onItemClick = {
                        if (isLyricScrollEnable.value) {
                            isLyricScrollEnable.value = false
                        }
                        PlayerAction.SeekTo(it.time).action()
                    },
                    onItemLongClick = {
                        if (draggable.state.value == DragAnchor.Max) {
                            isLyricScrollEnable.value = !isLyricScrollEnable.value
                        }
                    },
                )
            }
        },
        playlistContent = { modifier ->
            Surface(color = MaterialTheme.colors.background) {
                PlaylistLayout(
                    modifier = modifier.clipToBounds(),
                    forceRefresh = { draggable.state.value != DragAnchor.Min },
                    items = { MPlayer.currentTimelineItems }
                )
            }
        },
        overlayContent = {
            val animateProgress = animateFloatAsState(
                targetValue = if (!isLyricScrollEnable.value) 100f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = ""
            )

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        alpha = animateProgress.value / 100f
                        translationY = (1f - animateProgress.value / 100f) * 500f
                    }
            ) {
                SeekbarLayout(
                    modifier = Modifier.hideControl(enable = { hideComponent.value }),
                    animateColor = { animateColor.value },
                    onValueChange = { seekbarTime.longValue = it.toLong() },
                    maxValue = { MPlayer.currentDuration.toFloat() },
                    dataValue = { currentPosition.floatValue },
                    onDispatchDragOffset = { enhanceSheetState?.dispatch(it) },
                    onDragStop = { result ->
                        if (result == -1) enhanceSheetState?.hide()
                        else enhanceSheetState?.settle(0f)
                    },
                    onSeekTo = { position ->
                        PlayerAction.SeekTo(position.toLong()).action()
                    },
                    onSwitchTo = { index ->
                        val playMode = when (index) {
                            1 -> PlayMode.RepeatOne
                            2 -> PlayMode.Shuffle
                            else -> PlayMode.ListRecycle
                        }
                        PlayerAction.SetPlayMode(playMode)
                            .action()
                        DynamicTipsItem.Static(
                            title = when (playMode) {
                                PlayMode.ListRecycle -> "列表循环"
                                PlayMode.RepeatOne -> "单曲循环"
                                PlayMode.Shuffle -> "随机播放"
                            },
                            subTitle = "切换播放模式",
                        ).show()
                    },
                    onClick = { clickPart ->
                        when (clickPart) {
                            ClickPart.Start -> PlayerAction.SkipToPrevious.action()
                            ClickPart.Middle -> PlayerAction.PlayOrPause.action()
                            ClickPart.End -> PlayerAction.SkipToNext.action()
                        }
                    }
                )
            }
        }
    )
}