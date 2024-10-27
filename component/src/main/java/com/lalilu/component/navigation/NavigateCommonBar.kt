package com.lalilu.component.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachReversed
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.System
import com.lalilu.remixicon.arrows.arrowLeftSLine
import com.lalilu.remixicon.system.moreLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlin.random.nextInt


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

@Composable
private fun MoreActionBtn(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    dotColors: List<Color> = emptyList(),
    onClick: () -> Unit = {}
) {
    TextButton(
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        onClick = onClick
    ) {
        val showingColor = remember { mutableStateOf<Color?>(null) }

        LaunchedEffect(showingColor.value) {
            if (showingColor.value == null) {
                showingColor.value = dotColors.firstOrNull()
                return@LaunchedEffect
            }

            delay(3000)
            if (!isActive) return@LaunchedEffect

            val currentIndex = dotColors.indexOf(showingColor.value)
            val nextIndex = (currentIndex + 1) % dotColors.size
            showingColor.value = dotColors.getOrNull(nextIndex)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = RemixIcon.System.moreLine,
                contentDescription = null,
                tint = color
            )

            showingColor.value?.let { dotColor ->
                AnimatedContent(
                    modifier = Modifier.align(Alignment.TopStart),
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith
                                fadeOut(spring(stiffness = Spring.StiffnessLow))
                    },
                    targetState = dotColor,
                    label = ""
                ) {
                    Spacer(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = it)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ActionItem(
    modifier: Modifier = Modifier,
    actionContext: ActionContext,
    action: ScreenAction
) {
    when (action) {
        is ScreenAction.Dynamic -> {
            action.content(actionContext)
        }

        is ScreenAction.Static -> {
            val color = action.color()
            val title = action.title()
            val subTitle = action.subTitle()
            val icon = action.icon()
            val dotColor = action.dotColor()

            Surface(
                modifier = modifier,
                color = color.copy(0.2f),
                onClick = { action.onAction() }
            ) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        icon?.let {
                            Image(
                                modifier = Modifier.size(20.dp),
                                imageVector = icon,
                                contentDescription = title,
                                colorFilter = ColorFilter.tint(color = color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement
                                .spacedBy(2.dp, Alignment.CenterVertically)
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                color = color,
                                fontWeight = FontWeight.Medium
                            )

                            if (actionContext.isFullyExpanded && subTitle != null) {
                                Text(
                                    text = subTitle,
                                    fontSize = 10.sp,
                                    lineHeight = 10.sp,
                                    color = color.copy(0.5f),
                                )
                            }
                        }
                    }

                    if (dotColor != null) {
                        val animation = rememberInfiniteTransition(label = "")
                        val scaleValue = animation.animateFloat(
                            initialValue = 0.1f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1000),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(
                                    offsetMillis = remember { Random.nextInt(0..1000) }
                                )
                            ),
                            label = ""
                        )

                        Spacer(
                            modifier = Modifier
                                .graphicsLayer { alpha = scaleValue.value }
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(color = dotColor)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreActionPanelDialog(
    isVisible: MutableState<Boolean>,
    actions: List<ScreenAction>,
) {
    val actualActions = rememberUpdatedState(newValue = actions)

    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            MoreActionPanelDialogContent(
                actions = actualActions.value,
                onDismiss = { dismiss() }
            )
        }
    }

    DialogWrapper.register(
        isVisible = { isVisible.value },
        onDismiss = { isVisible.value = false },
        dialogItem = dialog
    )
}

@Composable
private fun MoreActionPanelDialogContent(
    modifier: Modifier = Modifier,
    actions: List<ScreenAction>,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .navigationBarsPadding(),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(0.1f)),
        shape = RoundedCornerShape(18.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            actions.forEach { action ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    ActionItem(
                        action = action,
                        actionContext = ActionContext(isFullyExpanded = true)
                    )
                }
            }
        }
    }
}
