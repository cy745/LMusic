package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.measureHeight
import kotlinx.coroutines.delay

object SmartFloatBtns {
    private val btnItems: MutableState<List<FloatBtnItem>> = mutableStateOf(emptyList())
    private val showAll: MutableState<Boolean> = mutableStateOf(false)

    val floatBtnsHeightDpState = mutableStateOf(0.dp)
    val colors = listOf(
        Color(0xFF09B1C7),
        Color(0xFFF82AB3),
        Color(0xFF256DFF),
        Color(0xFF13C94F),
        Color(0xFFE94C0F),
    )

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun BoxScope.SmartFloatBtnsContent(modifier: Modifier) {
        val density = LocalDensity.current

        LaunchedEffect(showAll.value, btnItems.value) {
            // 当没有按钮时，自动收起
            if (btnItems.value.isEmpty()) {
                showAll.value = false
                return@LaunchedEffect
            }
            // 展开后10s自动收起
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
                shape = CircleShape,
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .animateContentSize()
                        .padding(9.dp)
                        .measureHeight { _, height ->
                            floatBtnsHeightDpState.value = density.run { height.toDp() + 12.dp }
                        },
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(targetState = showAll.value) { isShowAll ->
                        IconItem(
                            painter = painterResource(id = if (isShowAll) R.drawable.ic_close_line else R.drawable.ic_more_fill),
                            title = "more",
                            color = colors[0],
                            onClick = { showAll.value = !showAll.value }
                        )
                    }
                    btnItems.value.forEachIndexed { index, item ->
                        AnimatedVisibility(visible = showAll.value) {
                            IconItem(
                                modifier = Modifier.padding(top = 9.dp),
                                painter = painterResource(id = item.icon),
                                title = item.title,
                                color = colors[(index + 1) % colors.size],
                                onClick = item.callback
                            )
                        }
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

    @Composable
    fun RegisterFloatBtns(items: List<FloatBtnItem>) {
        LaunchedEffect(items) { btnItems.value = items }
        DisposableEffect(Unit) {
            onDispose { btnItems.value = emptyList() }
        }
    }

    class FloatBtnItem(
        val icon: Int,
        val title: String,
        val callback: () -> Unit
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