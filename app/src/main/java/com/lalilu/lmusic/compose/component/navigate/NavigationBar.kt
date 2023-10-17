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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.lmusic.compose.CustomScreen
import com.lalilu.lmusic.compose.TabScreen
import com.lalilu.lmusic.compose.component.BottomSheetNavigator
import com.lalilu.lmusic.utils.extension.dayNightTextColor

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    tabScreens: () -> List<TabScreen>,
    navigator: BottomSheetNavigator,
) {
    val currentScreen by remember { derivedStateOf { navigator.lastItemOrNull } }
    val isCurrentTabScreen by remember { derivedStateOf { currentScreen as? TabScreen != null } }
    val previousScreen by remember(currentScreen) {
        derivedStateOf { navigator.items.getOrNull(navigator.size - 2) as? CustomScreen }
    }
    val previousInfo by remember { derivedStateOf { previousScreen?.getScreenInfo() } }
    val previousTitle by remember {
        derivedStateOf { previousInfo?.title ?: R.string.bottom_sheet_navigate_back }
    }

    AnimatedContent(
        modifier = modifier.fillMaxWidth(),
        targetState = isCurrentTabScreen,
        label = ""
    ) { tabScreenNow ->
        if (tabScreenNow) {
            NavigateTabBar(
                currentScreen = { currentScreen },
                tabScreens = tabScreens,
                navigator = navigator
            )
        } else {
            NavigateCommonBar(
                previousTitle = { previousTitle },
                navigator = navigator
            )
        }
    }
}

@Composable
fun NavigateTabBar(
    modifier: Modifier = Modifier,
    currentScreen: () -> Screen?,
    tabScreens: () -> List<TabScreen>,
    navigator: BottomSheetNavigator
) {
    Row(
        modifier = modifier
            .clickable(enabled = false) {}
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        tabScreens().forEach {
            NavigateItem(
                titleRes = { it.getScreenInfo().title },
                iconRes = { it.getScreenInfo().icon ?: R.drawable.ic_close_line },
                isSelected = { currentScreen() === it },
                onClick = { navigator.pushTab(it) }
            )
        }
    }
}

@Composable
fun NavigateCommonBar(
    modifier: Modifier = Modifier,
    previousTitle: () -> Int,
    navigator: BottomSheetNavigator
) {
    Row(
        modifier = modifier
            .clickable(enabled = false) {}
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val contentColor =
            contentColorFor(backgroundColor = MaterialTheme.colors.background)
        TextButton(
            contentPadding = PaddingValues(start = 8.dp, end = 16.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = contentColor
            ),
            onClick = {
                if (navigator.items.size == 1) {
                    navigator.hide()
                } else {
                    navigator.pop()
                }
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                contentDescription = "backButtonIcon",
                colorFilter = ColorFilter.tint(color = contentColor)
            )
            Text(
                text = stringResource(id = previousTitle()),
                fontSize = 14.sp
            )
        }

        TextButton(
            contentPadding = PaddingValues(horizontal = 20.dp),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color(0x25FE4141),
                contentColor = Color(0xFFFE4141)
            ),
            onClick = { navigator.hide() }
        ) {
            Text(
                text = stringResource(id = R.string.bottom_sheet_navigate_close),
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigateItem(
    titleRes: () -> Int,
    iconRes: () -> Int,
    isSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    baseColor: Color = MaterialTheme.colors.primary,
    unSelectedColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
) {
    val titleValue = stringResource(id = titleRes())
    val iconTintColor = animateColorAsState(if (isSelected()) baseColor else unSelectedColor)
    val backgroundColor by animateColorAsState(if (isSelected()) baseColor.copy(alpha = 0.12f) else Color.Transparent)

    Surface(
        color = backgroundColor,
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier
            .size(48.dp)
            .aspectRatio(1f)
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
