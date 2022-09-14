package com.lalilu.lmusic.screen.component.card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.lalilu.lmusic.utils.extension.dayNightTextColor

/**
 * 此组件的效果时在一定的宽度内保证文字单行显示，若无法在单行内显示完全则会省略显示对应溢出的部分
 * 改变 [expendedState] 即可使文字在竖直方向上展开省略掉的部分
 *
 * 最好使用Compose的 [IntrinsicSize] 来使父级设定按最小宽度布局，以此为本组件满足 “在一定的宽度内” 的条件前提
 *
 * @param title             主文字
 * @param subTitle          次要文字
 * @param defaultState      初始化时是否展开文字
 * @param expendedState     控制文字是否展开的状态
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpendableTextCard(
    title: String,
    subTitle: String,
    titleColor: Color = dayNightTextColor(),
    subTitleColor: Color = dayNightTextColor(0.5f),
    defaultState: Boolean = false,
    expendedState: MutableState<Boolean> = remember { mutableStateOf(defaultState) }
) {
    AnimatedContent(targetState = expendedState.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(MutableInteractionSource(), indication = null) {
                    expendedState.value = !it
                }
        ) {
            Text(
                maxLines = if (it) Int.MAX_VALUE else 1,
                overflow = if (it) TextOverflow.Visible else TextOverflow.Ellipsis,
                softWrap = it, text = title, style = MaterialTheme.typography.subtitle1,
                color = titleColor
            )
            Text(
                maxLines = if (it) Int.MAX_VALUE else 1,
                overflow = if (it) TextOverflow.Visible else TextOverflow.Ellipsis,
                softWrap = it, text = subTitle, style = MaterialTheme.typography.subtitle2,
                color = subTitleColor
            )
        }
    }
}