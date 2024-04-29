package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import com.lalilu.R
import com.lalilu.component.base.BottomSheetNavigator
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.LocalBottomSheetNavigator
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.TabScreen
import com.lalilu.component.extension.dayNightTextColor

sealed class NavigationBarState {
    data class ForTabScreen(val tabScreens: List<TabScreen>) : NavigationBarState()
    data class ForScreen(val screen: DynamicScreen?) : NavigationBarState()
}

@Composable
fun rememberNavigationBarState(
    tabScreens: () -> List<TabScreen>,
    currentScreen: () -> Screen?,
): State<NavigationBarState> {
    return remember {
        derivedStateOf {
            when (val screen = currentScreen()) {
                is TabScreen -> NavigationBarState.ForTabScreen(tabScreens())
                is DynamicScreen -> NavigationBarState.ForScreen(screen)
                else -> NavigationBarState.ForScreen(null)
            }
        }
    }
}

@Composable
fun rememberPreviousScreenTitleRes(
    stack: Stack<Screen>?,
    currentScreen: Screen?
): State<Int> {
    val previousScreen by remember(currentScreen) {
        derivedStateOf { stack?.items?.getOrNull(stack.size - 2) as? CustomScreen }
    }
    val previousInfo by remember {
        derivedStateOf { previousScreen?.getScreenInfo() }
    }
    return remember {
        derivedStateOf { previousInfo?.title ?: R.string.bottom_sheet_navigate_back }
    }
}


@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    tabScreens: () -> List<TabScreen>,
    currentScreen: () -> Screen?,
    navigator: BottomSheetNavigator? = LocalBottomSheetNavigator.current
) {
    val navigationBarState =
        rememberNavigationBarState(tabScreens = tabScreens, currentScreen = currentScreen)

    AnimatedContent(
        modifier = modifier.fillMaxWidth(),
        targetState = navigationBarState.value,
        label = "NavigateBarTransform"
    ) { state ->
        when (state) {
            is NavigationBarState.ForTabScreen -> {
                NavigateTabBar(
                    tabScreens = state::tabScreens::get,
                    currentScreen = currentScreen,
                    onSelectTab = { navigator?.jump(it) }
                )
            }

            is NavigationBarState.ForScreen -> {
                val actions = state.screen?.registerActions() ?: emptyList()
                val previousTitle = rememberPreviousScreenTitleRes(
                    stack = navigator,
                    currentScreen = currentScreen()
                )

                NavigateCommonBar(
                    previousTitle = { previousTitle.value },
                    screenActions = { actions }
                )
            }
        }
    }
}

@Composable
fun NavigateTabBar(
    modifier: Modifier = Modifier,
    currentScreen: () -> Screen?,
    tabScreens: () -> List<TabScreen>,
    onSelectTab: (TabScreen) -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(enabled = false) {}
            .height(52.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabScreens().forEach {
            NavigateItem(
                modifier = Modifier.weight(1f),
                titleRes = { it.getScreenInfo().title },
                iconRes = { it.getScreenInfo().icon ?: R.drawable.ic_close_line },
                isSelected = { currentScreen() === it },
                onClick = { onSelectTab(it) }
            )
        }
    }
}

@Composable
fun NavigateCommonBar(
    modifier: Modifier = Modifier,
    previousTitle: () -> Int,
    screenActions: () -> List<ScreenAction>,
    navigator: BottomSheetNavigator? = LocalBottomSheetNavigator.current
) {
    val itemFitImePadding = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(enabled = false) {}
            .run { if (itemFitImePadding.value) this.imePadding() else this }
            .height(52.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val contentColor =
            contentColorFor(backgroundColor = MaterialTheme.colors.background)
        TextButton(
            modifier = Modifier.fillMaxHeight(),
            shape = RectangleShape,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            onClick = { navigator?.back() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                contentDescription = "backButtonIcon",
                colorFilter = ColorFilter.tint(color = contentColor)
            )
            AnimatedContent(targetState = previousTitle(), label = "") {
                Text(
                    text = stringResource(id = it),
                    fontSize = 14.sp
                )
            }
        }

        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            targetState = screenActions(),
            label = "ExtraActions"
        ) { actions ->
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                items(items = actions) {
                    if (it is ScreenAction.ComposeAction) {
                        it.content.invoke()
                        return@items
                    }

                    if (it is ScreenAction.StaticAction) {
                        if (it.fitImePadding) {
                            LaunchedEffect(Unit) {
                                itemFitImePadding.value = true
                            }
                        }

                        TextButton(
                            modifier = Modifier.fillMaxHeight(),
                            shape = RectangleShape,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = it.color.copy(alpha = 0.15f),
                                contentColor = it.color
                            ),
                            onClick = it.onAction
                        ) {
                            it.icon?.let { icon ->
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(id = icon),
                                    contentDescription = stringResource(id = it.title),
                                    colorFilter = ColorFilter.tint(color = it.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = stringResource(id = it.title),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (actions.isEmpty()) {
                    item {
                        TextButton(
                            modifier = Modifier.fillMaxHeight(),
                            shape = RectangleShape,
                            contentPadding = PaddingValues(start = 20.dp, end = 28.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = Color(0x25FE4141),
                                contentColor = Color(0xFFFE4141)
                            ),
                            onClick = { navigator?.hide() }
                        ) {
                            Text(
                                text = stringResource(id = R.string.bottom_sheet_navigate_close),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigateItem(
    modifier: Modifier = Modifier,
    titleRes: () -> Int,
    iconRes: () -> Int,
    isSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    baseColor: Color = MaterialTheme.colors.primary,
    unSelectedColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
) {
    val titleValue = stringResource(id = titleRes())
    val iconTintColor = animateColorAsState(
        targetValue = if (isSelected()) baseColor else unSelectedColor,
        label = ""
    )
//    val backgroundColor by animateColorAsState(
//        targetValue = if (isSelected()) baseColor.copy(alpha = 0.12f) else Color.Transparent,
//        label = ""
//    )

    Surface(
        color = Color.Transparent,
        onClick = onClick,
        shape = RectangleShape,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = iconRes()),
                    contentDescription = titleValue,
                    colorFilter = ColorFilter.tint(iconTintColor.value),
                    contentScale = FixedScale(if (isSelected()) 1.1f else 1f)
                )
                AnimatedVisibility(visible = isSelected()) {
                    Text(
                        text = titleValue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.1.sp,
                        color = dayNightTextColor()
                    )
                }
            }
        }
    }
}
