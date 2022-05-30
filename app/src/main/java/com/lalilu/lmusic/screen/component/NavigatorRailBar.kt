package com.lalilu.lmusic.screen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.navigate

@Composable
fun NavigatorRailBar(
    navController: NavController,
    topExtraContent: LazyListScope.() -> Unit = {},
    bottomExtraContent: LazyListScope.() -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: MainScreenData.Library.name

    NavigatorRailBar(
        currentRoute = currentRoute,
        navigateTo = { navController.navigate(from = MainScreenData.Library.name, to = it) },
        topExtraContent, bottomExtraContent
    )
}

@Composable
fun NavigatorRailBar(
    currentRoute: String,
    navigateTo: (destination: String) -> Unit = {},
    topExtraContent: LazyListScope.() -> Unit = {},
    bottomExtraContent: LazyListScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .fillMaxHeight()
            .padding(start = 15.dp, end = 10.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.TopCenter),
            content = topExtraContent
        )
        LazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                NavigatorButton(
                    isSelected = currentRoute == MainScreenData.Library.name,
                    routeData = MainScreenData.Library,
                    navigateTo = navigateTo
                )
            }
            MainScreenData.values().forEach {
                if (it.showNavigateButton) {
                    item {
                        NavigatorButton(
                            isSelected = currentRoute.contains(it.name),
                            routeData = it,
                            navigateTo = navigateTo
                        )
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter),
            content = bottomExtraContent
        )
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun NavigatorButton(
    modifier: Modifier = Modifier,
    routeData: MainScreenData,
    isSelected: Boolean = false,
    baseColor: Color = MaterialTheme.colors.primary,
    navigateTo: (destination: String) -> Unit = {}
) {
    val imageAlpha = if (isSelected) 1f else 0.6f
    val iconTintColor = if (isSelected) baseColor else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
    }
    val backgroundColor by animateColorAsState(
        if (isSelected) baseColor.copy(alpha = 0.12f) else Color.Transparent
    )

    Surface(
        color = backgroundColor,
        onClick = { navigateTo(routeData.name) },
        shape = CircleShape,
        modifier = modifier.size(48.dp)
    ) {
        Image(
            painter = painterResource(id = routeData.icon),
            contentDescription = stringResource(id = routeData.title),
            colorFilter = ColorFilter.tint(iconTintColor),
            contentScale = FixedScale(1.1f),
            alpha = imageAlpha
        )
    }
}