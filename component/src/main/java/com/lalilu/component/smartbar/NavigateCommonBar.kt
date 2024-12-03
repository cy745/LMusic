package com.lalilu.component.smartbar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachReversed
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.smartbar.component.ActionItem
import com.lalilu.component.smartbar.component.MoreActionBtn
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.arrows.arrowLeftSLine


@Composable
fun NavigateCommonBar(
    modifier: Modifier = Modifier,
    previousTitle: String,
    currentScreen: Screen?
) {
    val screenActions = (currentScreen as? ScreenActionFactory)?.provideScreenActions()
    val actionContext = ActionContext(isFullyExpanded = false)
    val isDialogVisible = remember { mutableStateOf(false) }

    NavigateCommonBarContent(
        modifier = modifier,
        previousTitle = previousTitle,
        dialogVisible = isDialogVisible,
        screenActions = screenActions,
        actionContext = actionContext
    )
}

@Composable
fun NavigateCommonBarContent(
    modifier: Modifier = Modifier,
    previousTitle: String,
    previousIcon: ImageVector = RemixIcon.Arrows.arrowLeftSLine,
    dialogVisible: MutableState<Boolean>,
    screenActions: List<ScreenAction>?,
    actionContext: ActionContext,
    onBackPress: (() -> Unit)? = null
) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher

    MoreActionPanelDialog(
        isVisible = dialogVisible,
        actions = screenActions ?: emptyList()
    )

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            modifier = Modifier.fillMaxHeight(),
            shape = RectangleShape,
            contentPadding = PaddingValues(start = 12.dp, end = 20.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colors.onBackground
            ),
            onClick = {
                if (onBackPress != null) {
                    onBackPress()
                } else {
                    onBackPressedDispatcher?.onBackPressed()
                }
            }
        ) {
            Icon(
                imageVector = previousIcon,
                tint = MaterialTheme.colors.onBackground,
                contentDescription = null
            )
            AnimatedContent(
                targetState = previousTitle, label = ""
            ) {
                Text(
                    text = it,
                    fontSize = 14.sp
                )
            }
        }

        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            targetState = screenActions,
            label = "ExtraActions"
        ) { actions ->
            SubcomposeLayout(
                modifier = Modifier.fillMaxSize()
            ) { constraints ->
                // 若actions为空，则不显示
                if (actions == null) return@SubcomposeLayout layout(0, 0) {}

                val moreBtnMeasurable = subcompose("moreBtn") {
                    val colors = screenActions?.filterIsInstance<ScreenAction.Static>()
                        ?.mapNotNull { it.dotColor() }
                        ?: emptyList()

                    MoreActionBtn(
                        dotColors = colors,
                        onClick = { dialogVisible.value = true },
                    )
                }[0]
                val moreBtnPlaceable = moreBtnMeasurable.measure(
                    constraints.copy(
                        maxWidth = moreBtnMeasurable.maxIntrinsicWidth(constraints.maxWidth),
                        minWidth = 0
                    )
                )

                var widthSum = 0f
                val targets = mutableListOf<Placeable>()
                for (action in actions) {
                    val measurable = subcompose(action) {
                        ActionItem(
                            action = action,
                            actionContext = actionContext
                        )
                    }[0]
                    val placeable = measurable.measure(
                        constraints.copy(
                            maxWidth = measurable.maxIntrinsicWidth(constraints.maxWidth),
                            minWidth = 0
                        )
                    )

                    // 若宽度超出，则显示下拉菜单按钮
                    if (placeable.width + moreBtnPlaceable.width + widthSum > constraints.maxWidth) {
                        targets.add(moreBtnPlaceable)
                        break
                    }

                    targets.add(placeable)
                    widthSum += placeable.width
                }

                layout(width = constraints.maxWidth, height = constraints.maxHeight) {
                    var startX = constraints.maxWidth

                    targets.fastForEachReversed {
                        it.place(x = startX - it.width, y = 0)
                        startX -= it.width
                    }
                }
            }
        }
    }
}
