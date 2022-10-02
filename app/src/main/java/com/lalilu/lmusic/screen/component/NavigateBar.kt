package com.lalilu.lmusic.screen.component

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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.screen.ScreenData
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenData.values().forEach {
                if (it.showNavigateButton) {
                    NavigateItem(
                        navController = navController,
                        currentRoute = currentRoute,
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
                            navController = navController,
                            currentRoute = currentRoute,
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
    navController: NavController,
    routeData: ScreenData,
    currentRoute: String?,
    baseColor: Color = MaterialTheme.colors.primary,
) {
    val icon = painterResource(id = routeData.icon)
    val title = stringResource(id = routeData.title)
    val selected = currentRoute?.contains(routeData.name) ?: false
    val imageAlpha = if (selected) 1f else 0.6f
    val iconTintColor = if (selected) baseColor else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    val backgroundColor by animateColorAsState(
        if (selected) baseColor.copy(alpha = 0.12f) else Color.Transparent
    )

    Surface(
        color = backgroundColor,
        onClick = {
            if (currentRoute == routeData.name) {
                routeData.isChecked?.let { it.value = !it.value }
            }
            navController.navigateSingleTop(routeData.name)
        },
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
                    painter = icon,
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(iconTintColor),
                    alpha = imageAlpha,
                    contentScale = FixedScale(if (selected) 1.1f else 1f)
                )
                AnimatedVisibility(visible = selected) {
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
