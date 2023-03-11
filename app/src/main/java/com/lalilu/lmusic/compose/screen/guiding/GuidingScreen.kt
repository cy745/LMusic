package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.R
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.rememberIsPad

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun GuidingScreen(
    navController: NavHostController = rememberAnimatedNavController()
) {
    val windowSize = LocalWindowSize.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val destination = currentBackStack?.destination?.route.let {
        GuidingNavGraph.fromRoute(it)
    }

    val isPad by windowSize.rememberIsPad()

    Surface(color = MaterialTheme.colors.background) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = if (isPad) {
                    Modifier.width(screenHeightDp / 2f)
                } else {
                    Modifier.fillMaxWidth()
                }
                    .fillMaxHeight()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(visible = (GuidingNavGraph.getIndex(destination) != 0)) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                                contentDescription = "navigateUp"
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        AnimatedContent(targetState = destination, transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn() with
                                    slideOutVertically { height -> -height } + fadeOut()).using(
                                SizeTransform(clip = false)
                            )
                        }) {
                            Text(text = it?.title ?: "", fontSize = 22.sp)
                        }
                        Text(
                            text = "${GuidingNavGraph.getIndex(destination) + 1} / ${GuidingNavGraph.values().size}",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                AnimatedNavHost(
                    navController = navController,
                    startDestination = GuidingNavGraph.Agreement.name,
                    exitTransition = { ExitTransition.None },
                    enterTransition = {
                        fadeIn(animationSpec = tween(durationMillis = 300)) +
                                slideInVertically { 100 }
                    }
                ) {
                    composable(
                        route = GuidingNavGraph.Agreement.name
                    ) {
                        AgreementPage(navController)
                    }
                    composable(
                        route = GuidingNavGraph.Permissions.name
                    ) {
                        PermissionsPage(navController)
                    }
                    composable(
                        route = GuidingNavGraph.SeekbarGuiding.name
                    ) {
                        SeekbarGuidingPage()
                    }
                }
            }
        }
    }
}

@Composable
fun CheckActionCard(
    isPassed: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Surface(shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            content()
            Crossfade(targetState = isPassed) {
                if (it) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_line),
                        contentDescription = "",
                        tint = Color(0xFF3EA22C)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_checkbox_blank_circle_line),
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    text: String? = null,
    rejectTitle: String = "拒绝",
    confirmTitle: String = "确认",
    onReject: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    getText: () -> String = { "" },
) {
    Surface(shape = RoundedCornerShape(10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text ?: getText().trimIndent(),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Row(modifier = Modifier.align(Alignment.End)) {
                onReject?.let {
                    TextButton(onClick = it) {
                        Text(text = rejectTitle)
                    }
                }
                onConfirm?.let {
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF3EA22C)
                        )
                    ) {
                        Text(text = confirmTitle)
                    }
                }
            }
        }
    }
}

enum class GuidingNavGraph(
    val title: String
) {
    Agreement(title = "用户协议 | 隐私协议"),
    Permissions(title = "必要权限授予"),
    SeekbarGuiding(title = "基础引导教程");

    fun getNext(): GuidingNavGraph? {
        return getNext(this)
    }

    fun navigate(navController: NavController) {
        navController.navigate(this.name)
    }

    companion object {
        fun fromRoute(route: String?): GuidingNavGraph? {
            val target = route?.substringBefore("/")
            return values().find { it.name == target }
        }

        fun fromIndex(index: Int): GuidingNavGraph? {
            return values().getOrNull(index)
        }

        fun getIndex(destination: GuidingNavGraph?): Int {
            destination ?: return -1
            return values().indexOf(destination)
        }

        fun getNext(destination: GuidingNavGraph?): GuidingNavGraph? {
            return fromIndex(getIndex(destination) + 1)
        }
    }
}
