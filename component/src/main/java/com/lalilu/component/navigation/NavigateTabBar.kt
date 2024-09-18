package com.lalilu.component.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.R
import com.lalilu.component.base.TabScreen
import com.lalilu.component.base.screen.ScreenInfoFactory


@Composable
fun NavigateTabBar(
    modifier: Modifier = Modifier,
    currentScreen: () -> Screen?,
    tabScreens: () -> List<TabScreen>,
    onSelectTab: (TabScreen) -> Unit = {}
) {
    val defaultTitle = stringResource(id = R.string.empty_screen_no_items)

    Row(
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabScreens().forEach {
            val screenInfo = (it as? ScreenInfoFactory)?.provideScreenInfo()
            val title = screenInfo?.title?.invoke()

            NavigateItem(
                modifier = Modifier.weight(1f),
                title = { title ?: defaultTitle },
                icon = { screenInfo?.icon },
                isSelected = { currentScreen() === it },
                onClick = { onSelectTab(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigateItem(
    modifier: Modifier = Modifier,
    title: () -> String,
    icon: () -> ImageVector?,
    isSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    baseColor: Color = MaterialTheme.colors.primary,
    unSelectedColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
) {
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
                icon()?.let {
                    Image(
                        imageVector = it,
                        contentDescription = title(),
                        colorFilter = ColorFilter.tint(iconTintColor.value),
                        contentScale = FixedScale(if (isSelected()) 1.1f else 1f)
                    )
                }
                AnimatedVisibility(visible = isSelected()) {
                    Text(
                        text = title(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.1.sp,
                        color = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    }
}