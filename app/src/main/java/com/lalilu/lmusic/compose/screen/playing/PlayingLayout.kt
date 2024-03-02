package com.lalilu.lmusic.compose.screen.playing

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.dirror.lyricviewx.LyricUtil
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lalilu.common.HapticUtils
import com.lalilu.component.extension.hideControl
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmusic.compose.component.playing.LyricViewToolbar
import com.lalilu.lmusic.compose.component.playing.PlayingToolbar
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.extension.SleepTimerSmallEntry
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import org.koin.compose.koinInject
import kotlin.math.pow
import kotlin.math.roundToInt


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun PlayingLayout(
    playingVM: PlayingViewModel = singleViewModel(),
    settingsSp: SettingsSp = koinInject()
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val systemUiController = rememberSystemUiController()
    val lyricLayoutLazyListState = rememberLazyListState()

    val isLyricScrollEnable = remember { mutableStateOf(false) }
    val recyclerViewScrollState = remember { mutableStateOf(false) }
    val backgroundColor = remember { mutableStateOf(Color.DarkGray) }
    val animateColor = animateColorAsState(targetValue = backgroundColor.value, label = "")
    val scrollToTopEvent = remember { mutableStateOf(0L) }

    val seekbarTime = remember { mutableLongStateOf(0L) }
    val draggable = rememberCustomAnchoredDraggableState { oldState, newState ->
        if (newState == DragAnchor.MiddleXMax && oldState != DragAnchor.MiddleXMax) {
            HapticUtils.haptic(view, HapticUtils.Strength.HAPTIC_STRONG)
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

    BackHandler(draggable.state.value == DragAnchor.Max) {
        if (draggable.state.value == DragAnchor.Max) {
            draggable.animateToState(DragAnchor.Middle)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                // 若非RecyclerView的滚动，则消费y轴上的所有速度，避免嵌套滚动事件继续
                if (!recyclerViewScrollState.value && !isLyricScrollEnable.value) {
                    draggable.fling(available.y)
                    return available
                }

                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (consumed.y != 0f && available.y == 0f) {
                    draggable.fling(0f)
                }
                return super.onPostFling(consumed, available)
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 取消正在进行的动画事件
                draggable.tryCancel()

                return when {
                    lyricLayoutLazyListState.isScrollInProgress -> {
                        if (
                            !isLyricScrollEnable.value
                            && available.y > 0
                            && source == NestedScrollSource.Drag
                            && draggable.position.floatValue.toInt()
                            == draggable.getPositionByAnchor(DragAnchor.Max)
                        ) {
                            HapticUtils.haptic(view, HapticUtils.Strength.HAPTIC_STRONG)
                            isLyricScrollEnable.value = true
                        }

                        if (isLyricScrollEnable.value) {
                            super.onPreScroll(available, source)
                        } else {
                            if (source == NestedScrollSource.Drag) {
                                draggable.scrollBy(available.y)
                            }
                            available
                        }
                    }

                    recyclerViewScrollState.value -> {
                        if (available.y < 0) available.copy(y = draggable.scrollBy(available.y))
                        else super.onPreScroll(available, source)
                    }

                    // 前面的条件都不满足，则将该事件全部消费，避免未知的子组件产生动作
                    else -> available
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return when {
                    lyricLayoutLazyListState.isScrollInProgress -> {
                        if (isLyricScrollEnable.value) {
                            super.onPreScroll(available, source)
                        } else {
                            draggable.scrollBy(available.y)
                            available
                        }
                    }

                    recyclerViewScrollState.value -> {
                        if (available.y > 0) available.copy(y = draggable.scrollBy(available.y))
                        else super.onPostScroll(consumed, available, source)
                    }

                    else -> super.onPostScroll(consumed, available, source)
                }
            }
        }
    }

    BoxWithConstraints {
        Layout(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection),
            content = {
                Column(
                    modifier = Modifier
                        .hideControl(
                            enable = { hideComponent.value },
                            intercept = { true }
                        )
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 10.dp)
                ) {
                    PlayingToolbar(
                        isItemPlaying = { mediaId -> playingVM.isItemPlaying { it.mediaId == mediaId } },
                        isUserTouchEnable = { draggable.state.value == DragAnchor.Min || draggable.state.value == DragAnchor.Max },
                        isExtraVisible = { draggable.state.value == DragAnchor.Max },
                        onClick = { scrollToTopEvent.value = System.currentTimeMillis() },
                        fixContent = { SleepTimerSmallEntry() },
                        extraContent = { LyricViewToolbar() }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            clipRect(0f, 0f, size.width, draggable.position.floatValue) {
                                drawRect(animateColor.value)
                                this@drawWithContent.drawContent()
                            }
                        }
                ) {
                    val adInterpolator = remember { AccelerateDecelerateInterpolator() }
                    val dInterpolator = remember { DecelerateInterpolator() }
                    val transition: (Float) -> Float = remember {
                        { x -> -2f * (x - 0.5f).pow(2) + 0.5f }
                    }

                    val lyricEntry = playingVM.lyricRepository.currentLyric
                        .mapLatest {
                            LyricUtil
                                .parseLrc(arrayOf(it?.first, it?.second))
                                ?.mapIndexed { index, lyricEntry ->
                                    LyricEntry(
                                        index = index,
                                        time = lyricEntry.time,
                                        text = lyricEntry.text,
                                        translate = lyricEntry.secondText
                                    )
                                }
                                ?: emptyList()
                        }
                        .collectAsState(initial = emptyList())
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
                                val progress = middleToMaxProgress.value
                                val dProgress = dInterpolator.getInterpolation(progress)
                                val minTop =
                                    (this@BoxWithConstraints.constraints.maxHeight - this@BoxWithConstraints.constraints.maxWidth) / 2f
                                val offsetTop = lerp(0f, minTop, dProgress)

                                val aspectRatio =
                                    this@BoxWithConstraints.constraints.maxHeight.toFloat() / this@BoxWithConstraints.constraints.maxWidth.toFloat()
                                val scale = lerp(1f, aspectRatio, progress)

                                val floatProgress = transition(middleToMaxProgress.value)
                                val translation = floatProgress * 200f

                                alpha = minToMiddleProgress.value
                                translationY = offsetTop + translation
                                scaleY = scale
                                scaleX = scale
                            },
                        blurProgress = { middleToMaxProgress.value },
                        onBackgroundColorFetched = { backgroundColor.value = it },
                        imageData = {
                            playingVM.playing.value
                                ?: com.lalilu.component.R.drawable.ic_music_2_line_100dp
                        }
                    )

                    LyricLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val interpolation =
                                    adInterpolator.getInterpolation(middleToMaxProgress.value)
                                val progressIncrease = (2 * interpolation - 1F).coerceAtLeast(0F)

                                val floatProgress = transition(middleToMaxProgress.value)
                                val translation = floatProgress * 200f

                                translationY = translation * 3f
                                alpha = progressIncrease
                            },
                        lyricEntry = lyricEntry,
                        listState = lyricLayoutLazyListState,
                        currentTime = { seekbarTime.longValue },
                        maxWidth = { this@BoxWithConstraints.constraints.maxWidth },
                        textSize = rememberTextSizeFromInt { settingsSp.lyricTextSize.value },
                        textAlign = rememberTextAlignFromGravity { settingsSp.lyricGravity.value },
                        fontFamily = rememberFontFamilyFromPath { settingsSp.lyricTypefacePath.value },
                        isBlurredEnable = { !isLyricScrollEnable.value && settingsSp.isEnableBlurEffect.value },
                        isTranslationShow = { settingsSp.isDrawTranslation.value },
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
                            LPlayer.controller.doAction(PlayerAction.SeekTo(it.time))
                        },
                        onItemLongClick = {
                            if (draggable.state.value == DragAnchor.Max) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isLyricScrollEnable.value = !isLyricScrollEnable.value
                            }
                        },
                    )
                }

                CustomRecyclerView(
                    modifier = Modifier.clipToBounds(),
                    scrollToTopEvent = { scrollToTopEvent.value },
                    onScrollStart = { recyclerViewScrollState.value = true },
                    onScrollTouchUp = { },
                    onScrollIdle = {
                        recyclerViewScrollState.value = false
                        draggable.fling(0f)
                    }
                )
            }
        ) { measurables, constraints ->
            val minHeader = measurables[0].measure(constraints)

            val picture = measurables[1].measure(constraints)

            val cConstraints =
                constraints.copy(maxHeight = constraints.maxHeight - minHeader.height)
            val column = measurables[2].measure(cConstraints)

            draggable.updateAnchor(
                min = minHeader.height,
                middle = constraints.maxWidth,
                max = constraints.maxHeight
            )

            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight
            ) {
                val animateOffset = draggable.position.floatValue.roundToInt()
                    .coerceIn(minHeader.height, constraints.maxHeight)

                picture.place(0, 0)
                minHeader.place(0, animateOffset - minHeader.height)
                column.place(0, animateOffset)
            }
        }

        val animateProgress = animateFloatAsState(
            targetValue = if (!isLyricScrollEnable.value) 100f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = ""
        )

        SeekbarLayout(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    alpha = animateProgress.value / 100f
                    translationY = (1f - animateProgress.value / 100f) * 500f
                },
            seekBarModifier = Modifier.hideControl(enable = { hideComponent.value }),
            onValueChange = { seekbarTime.longValue = it },
            animateColor = animateColor
        )
    }
}