package com.lalilu.lmusic.compose.screen.guiding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import com.lalilu.R
import com.lalilu.component.base.CustomScreen
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.extension.rememberIsPad
import com.lalilu.component.navigation.CustomTransition

@Composable
fun GuidingScreen() {
    val windowSize = LocalWindowSize.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateTopPadding()

    val isPad by windowSize.rememberIsPad()
    val navigatorState = remember { mutableStateOf<Navigator?>(null) }
    val backStackSize = remember { derivedStateOf { navigatorState.value?.items?.size ?: 0 } }
    val showPopUpBtn = remember { derivedStateOf { backStackSize.value > 1 } }
    val currentScreenTitleRes by remember {
        derivedStateOf {
            (navigatorState.value?.lastItemOrNull as? CustomScreen)
                ?.getScreenInfo()
                ?.title
                ?: R.string.app_name
        }
    }

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
                    AnimatedVisibility(visible = showPopUpBtn.value) {
                        IconButton(onClick = { navigatorState.value?.pop() }) {
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
                        AnimatedContent(targetState = currentScreenTitleRes, transitionSpec = {
                            ((slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())).using(
                                SizeTransform(clip = false)
                            )
                        }, label = "") {
                            Text(text = stringResource(id = it), fontSize = 22.sp)
                        }
                        Text(
                            text = "${backStackSize.value} / 3",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
                Navigator(
                    AgreementScreen(
                        nextScreen = PermissionsScreen()
                    )
                ) { navigator ->
                    navigatorState.value = navigator
                    CustomTransition(
                        navigator = navigator
                    )
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
            Crossfade(targetState = isPassed, label = "") {
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
