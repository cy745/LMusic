package com.lalilu.lmusic.compose.component.navigate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.navigateSingleTop


@Composable
fun NavigateBar(horizontal: Boolean = true) {
    val navController = LocalNavigatorHost.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    if (horizontal) {
        Row(
            modifier = Modifier
                .height(52.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ScreenData.values().forEach {
                if (it.showNavigateButton) {
                    NavigateItem(
                        onClick = { navController.navigateSingleTop(it.name) },
                        getSelected = { currentRoute?.contains(it.name) ?: false },
                        routeData = it
                    )
                }
            }
        }
    } else {
        Box(
            Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .padding(start = 15.dp, end = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ScreenData.values().forEach {
                    if (it.showNavigateButton) {
                        NavigateItem(
                            onClick = { navController.navigateSingleTop(it.name) },
                            getSelected = { currentRoute?.contains(it.name) ?: false },
                            routeData = it
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigateItem(
    routeData: ScreenData,
    getSelected: () -> Boolean = { false },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    baseColor: Color = MaterialTheme.colors.primary,
    unSelectedColor: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
) {
    val title = stringResource(id = routeData.title)

    val iconTintColor = animateColorAsState(if (getSelected()) baseColor else unSelectedColor)
    val backgroundColor by animateColorAsState(if (getSelected()) baseColor.copy(alpha = 0.12f) else Color.Transparent)

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
                    painter = painterResource(id = routeData.icon),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(iconTintColor.value),
                    contentScale = FixedScale(if (getSelected()) 1.1f else 1f)
                )
                AnimatedVisibility(visible = getSelected()) {
                    Text(
                        text = title,
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
