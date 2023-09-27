package com.lalilu.lmusic.compose.new_screen

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lalilu.R
import com.lalilu.lmusic.compose.BottomSheetWrapper
import com.lalilu.lmusic.compose.new_screen.destinations.Destination
import com.lalilu.lmusic.compose.new_screen.destinations.PlaylistsScreenDestination
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.popUpElse
import com.lalilu.lmusic.utils.extension.rememberIsPad
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.destination

object NavBar {

    private val navItems = listOf(
        ScreenData.Home,
        ScreenData.Favourite,
        ScreenData.Playlists,
        ScreenData.Search
    )
    val content: @Composable () -> Unit by lazy { { Content() } }
    val verticalContent: @Composable () -> Unit by lazy { { VerticalContent() } }

    private enum class Type {
        Main, Extra, Hide
    }

    private fun NavController.navTo(
        currentDestinationSpec: Destination?,
        targetDestinationSpec: Destination
    ) {
        if (currentDestinationSpec == targetDestinationSpec) return

        if (targetDestinationSpec is DirectionDestinationSpec) {
            navigate(targetDestinationSpec) {
                popUpTo(graph.findStartDestination().id) {
                    inclusive = false
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        if (targetDestinationSpec is PlaylistsScreenDestination) {
            navigate(targetDestinationSpec()) {
                popUpTo(graph.findStartDestination().id) {
                    inclusive = false
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    @Composable
    private fun VerticalContent(navController: NavController = LocalNavigatorHost.current) {
        val destination by navController.appCurrentDestinationAsState()
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
                navItems.forEach {
                    NavigateItem(
                        titleRes = { it.title },
                        iconRes = { it.icon },
                        isSelected = { it.destination == destination },
                        onClick = { navController.navTo(destination, it.destination) }
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(navController: NavController = LocalNavigatorHost.current) {
        val haptic = LocalHapticFeedback.current
        val destination by navController.appCurrentDestinationAsState()
        val previousDestination = remember(destination) {
            val dest = navController.previousBackStackEntry?.destination()
            (dest as? Destination)?.let { ScreenData.getOrNull(it) }
        }

        val configuration = LocalConfiguration.current
        val isPad by LocalWindowSize.current.rememberIsPad()
        val isLandscape = remember(configuration.orientation) {
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        val showType by remember(isPad, isLandscape) {
            derivedStateOf {
                if (navItems.any { it.destination == destination }) {
                    if (isPad && isLandscape) Type.Hide else Type.Main
                } else {
                    Type.Extra
                }
            }
        }

        AnimatedContent(targetState = showType, label = "") { type ->
            if (type == Type.Main) {
                Row(
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    navItems.forEach {
                        NavigateItem(
                            titleRes = { it.title },
                            iconRes = { it.icon },
                            isSelected = { it.destination == destination },
                            onClick = { navController.navTo(destination, it.destination) }
                        )
                    }
                }
            } else if (type == Type.Extra) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .height(52.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val contentColor =
                        contentColorFor(backgroundColor = MaterialTheme.colors.background)
//                    onLongPress = {
//                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                        navController.popBackStack(
//                            HomeScreenDestination.route,
//                            false
//                        )
//                    }
                    // TODO 添加长按按钮返回首页的逻辑
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor
                        ),
                        onClick = {
                            navController.popUpElse {
                                BottomSheetWrapper.hide()
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                            contentDescription = "backButtonIcon",
                            colorFilter = ColorFilter.tint(color = contentColor)
                        )
                        Text(
                            text = previousDestination
                                ?.let { stringResource(id = it.title) }
                                ?: "返回",
                            fontSize = 14.sp
                        )
                    }
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = Color(0x25FE4141),
                            contentColor = Color(0xFFFE4141)
                        ),
                        onClick = { BottomSheetWrapper.hide() }
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_bottom_sheet_navigator_close),
                            fontSize = 14.sp
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
