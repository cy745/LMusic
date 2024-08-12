package com.lalilu.component.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lalilu.component.R
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenBarComponent
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.toColorFilter


sealed interface NavigationBarType {
    data object TabBar : NavigationBarType
    data object CommonBar : NavigationBarType
    data class NormalBar(val barComponent: ScreenBarComponent) : NavigationBarType
}

@Composable
fun NavigationSmartBar(
    modifier: Modifier = Modifier,
) {
    val currentScreen = LocalNavigator.current
        ?.currentScreen()
        ?.value

    val previousScreen = LocalNavigator.current
        ?.previousScreen()
        ?.value

    val previousTitle = (previousScreen as? ScreenInfoFactory)?.provideScreenInfo()
        ?.let { stringResource(id = it.title) }
        ?: "返回"

    val mainContent = (currentScreen as? ScreenBarFactory)?.content()
    val tabScreenRoutes = remember {
        listOf("/pages/home", "/pages/playlist", "/pages/search")
    }

    val tabScreens = remember(tabScreenRoutes) {
        tabScreenRoutes.mapNotNull { AppRouter.route(it).get() as? TabScreen }
    }

    val navigationBar: NavigationBarType = remember(mainContent, currentScreen) {
        when {
            mainContent != null -> NavigationBarType.NormalBar(mainContent)
            currentScreen is TabScreen -> NavigationBarType.TabBar
            else -> NavigationBarType.CommonBar
        }
    }

    AnimatedContent(
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) togetherWith slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down)
        },
        contentAlignment = Alignment.BottomCenter,
        targetState = navigationBar,
        label = ""
    ) { item ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background.copy(0.95f))
                .navigationBarsPadding()
                .height(56.dp)
        ) {
            when (item) {
                is NavigationBarType.NormalBar -> {
                    item.barComponent.content()
                }

                is NavigationBarType.TabBar -> {
                    NavigateTabBar(
                        currentScreen = { currentScreen },
                        tabScreens = { tabScreens },
                        onSelectTab = { AppRouter.intent(NavIntent.Jump(it)) }
                    )
                }

                is NavigationBarType.CommonBar -> {
                    NavigateCommonBar(
                        modifier = Modifier.fillMaxSize(),
                        previousTitle = previousTitle,
                        currentScreen = currentScreen
                    )
                }
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
            val screenInfo = (it as? ScreenInfoFactory)
                ?.provideScreenInfo()

            NavigateItem(
                modifier = Modifier.weight(1f),
                titleRes = { screenInfo?.title ?: R.string.empty_screen_no_items },
                iconRes = { screenInfo?.icon ?: R.drawable.ic_close_line },
                isSelected = { currentScreen() === it },
                onClick = { onSelectTab(it) }
            )
        }
    }
}

@Composable
fun NavigateCommonBar(
    modifier: Modifier = Modifier,
    previousTitle: String,
    currentScreen: Screen?
) {
    val itemFitImePadding = remember { mutableStateOf(false) }
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val screenActions = (currentScreen as? ScreenActionFactory)?.provideScreenActions()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = false) {}
            .run { if (itemFitImePadding.value) this.imePadding() else this },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            modifier = Modifier.fillMaxHeight(),
            shape = RectangleShape,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colors.onBackground
            ),
            onClick = { onBackPressedDispatcher?.onBackPressed() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                contentDescription = "backButtonIcon",
                colorFilter = MaterialTheme.colors.onBackground.toColorFilter()
            )
            AnimatedContent(targetState = previousTitle, label = "") {
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
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                items(items = actions ?: emptyList()) {
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