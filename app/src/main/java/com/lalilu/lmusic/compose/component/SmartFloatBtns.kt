package com.lalilu.lmusic.compose.component

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.measureHeight
import kotlinx.coroutines.delay

object SmartFloatBtns {
    private val btnItems: MutableState<List<FloatBtnItem>> = mutableStateOf(emptyList())
    private val showAll: MutableState<Boolean> = mutableStateOf(false)
    private val lastTouchTime: MutableState<Long> = mutableStateOf(0L)
    private var progressState: State<Float>? = null

    private val actualHeightDpState = mutableStateOf(0.dp)
    val floatBtnsHeightDpState = derivedStateOf {
        if (btnItems.value.isNotEmpty()) actualHeightDpState.value else 0.dp
    }

    val colors = listOf(
        Color(0xFF09B1C7),
        Color(0xFFF82AB3),
        Color(0xFF256DFF),
        Color(0xFF13C94F),
        Color(0xFFE94C0F),
    )

    @SuppressLint("UnnecessaryComposedModifier")
    @OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    fun BoxScope.SmartFloatBtnsContent(modifier: Modifier) {
        val density = LocalDensity.current
        val borderColor = animateColorAsState(
            if (MaterialTheme.colors.isLight) Color.Transparent else Color.DarkGray
        )

        // lastTouchTime 用于触发重启协程，即点击后重新开始计时
        LaunchedEffect(showAll.value, btnItems.value, lastTouchTime.value) {
            // 当没有按钮时，自动收起
            if (btnItems.value.isEmpty()) {
                showAll.value = false
                return@LaunchedEffect
            }
            // 若已展开，则10s自动收起
            if (showAll.value) {
                delay(10000)
                showAll.value = false
            }
        }

        AnimatedVisibility(
            modifier = modifier
                .align(Alignment.BottomEnd),
            visible = btnItems.value.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = SmartBar.smartBarHeightDpState.value)
                    .padding(end = 22.dp, bottom = 12.dp),
                border = BorderStroke(1.dp, borderColor.value),
                shape = CircleShape,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
                        // 通过修改高度来控制是否展开
                        .composed {
                            if (showAll.value) wrapContentHeight() else height(60.dp)
                        }
                        .wrapContentWidth()
                        .padding(9.dp)
                        .measureHeight { _, height ->
                            actualHeightDpState.value = density.run { height.toDp() + 12.dp }
                        }
                        // 在父级监听所有的TouchEvent，但是不拦截，只用于判断用户是否点击过该区域
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                lastTouchTime.value = System.currentTimeMillis()
                            }
                            false
                        },
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        modifier = Modifier
                            .drawBehind {
                                if (progressState != null) {
                                    val border = 3.dp
                                    drawArc(
                                        color = Color(0xD3A2A2A2),
                                        startAngle = -90f,
                                        sweepAngle = 360f * progressState!!.value,
                                        useCenter = false,
                                        size = Size(
                                            border.toPx() + this.size.width,
                                            border.toPx() + this.size.height
                                        ),
                                        topLeft = Offset(
                                            -(border / 2f).toPx(),
                                            -(border / 2f).toPx()
                                        ),
                                        style = Stroke(
                                            width = border.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            },
                        targetState = showAll.value,
                        transitionSpec = { fadeIn() with fadeOut() }
                    ) { isShowAll ->
                        IconItem(
                            painter = painterResource(id = if (isShowAll) R.drawable.ic_close_line else R.drawable.ic_more_fill),
                            title = "more",
                            color = colors[0],
                            onClick = { showAll.value = !showAll.value }
                        )
                    }
                    btnItems.value.forEachIndexed { index, item ->
                        IconItem(
                            modifier = Modifier.padding(top = 9.dp),
                            painter = painterResource(id = item.icon),
                            title = item.title,
                            color = colors[(index + 1) % colors.size],
                            onClick = { item.callback(showAll) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun IconItem(
        modifier: Modifier = Modifier,
        painter: Painter,
        title: String,
        color: Color,
        onClick: () -> Unit = {}
    ) {
        Surface(
            modifier = modifier,
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            IconButton(
                modifier = Modifier.size(42.dp),
                onClick = onClick
            ) {
                Icon(
                    tint = color,
                    painter = painter,
                    contentDescription = title
                )
            }
        }
    }

    private val contentStack = ArrayDeque<StackItem>()

    @Composable
    fun RegisterFloatBtns(progress: State<Float>? = null, items: List<FloatBtnItem>) {
        val stackItem = remember {
            StackItem(
                progress = progress,
                items = items
            )
        }
        LaunchedEffect(items) {
            if (!contentStack.contains(stackItem)) {
                contentStack.addLast(stackItem)
                stackItem.let {
                    btnItems.value = it.items
                    progressState = it.progress
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (contentStack.lastOrNull() == stackItem) {
                    contentStack.removeLast()
                    val lastOne = contentStack.lastOrNull()
                    btnItems.value = lastOne?.items ?: emptyList()
                    progressState = lastOne?.progress
                } else {
                    contentStack.remove(stackItem)
                }
            }
        }
    }

    private class StackItem(
        val progress: State<Float>? = null,
        val items: List<FloatBtnItem>
    )

    class FloatBtnItem(
        val icon: Int,
        val title: String,
        val callback: (showAll: MutableState<Boolean>) -> Unit
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FloatBtnItem

            if (icon != other.icon) return false
            if (title != other.title) return false
            if (callback != other.callback) return false

            return true
        }

        override fun hashCode(): Int {
            var result = icon
            result = 31 * result + title.hashCode()
            result = 31 * result + callback.hashCode()
            return result
        }
    }
}