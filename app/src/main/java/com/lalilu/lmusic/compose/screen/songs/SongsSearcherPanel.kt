package com.lalilu.lmusic.compose.screen.songs

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.remixicon.Arrows
import com.lalilu.remixicon.System
import com.lalilu.remixicon.arrows.arrowLeftSLine
import com.lalilu.remixicon.system.closeLine

@Composable
internal fun ScreenBarFactory.SongsSearcherPanel(
    isVisible: MutableState<Boolean>,
    keyword: () -> String,
    onUpdateKeyword: (String) -> Unit
) {
    RegisterContent(isVisible = isVisible, onBackPressed = { }) {
        SongsSearcherPanelContent(
            modifier = Modifier,
            keyword = keyword,
            onUpdateKeyword = onUpdateKeyword,
            onBackPress = { isVisible.value = false }
        )
    }
}

@Composable
private fun SongsSearcherPanelContent(
    modifier: Modifier = Modifier,
    keyword: () -> String,
    onUpdateKeyword: (String) -> Unit,
    onBackPress: (() -> Unit)? = null
) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            modifier = Modifier.fillMaxHeight(),
            shape = RectangleShape,
            contentPadding = PaddingValues(start = 12.dp, end = 20.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colors.onBackground
            ),
            onClick = {
                keyboard?.hide()

                if (onBackPress != null) {
                    onBackPress()
                } else {
                    onBackPressedDispatcher?.onBackPressed()
                }
            }
        ) {
            Icon(
                imageVector = RemixIcon.Arrows.arrowLeftSLine,
                tint = MaterialTheme.colors.onBackground,
                contentDescription = null
            )
            Text(
                text = "关闭",
                fontSize = 14.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colors.onBackground,
            )
        }

        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colors.onBackground.copy(0.05f)),
            value = keyword(),
            onValueChange = onUpdateKeyword,
            singleLine = true,
            maxLines = 1,
            cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp,
                lineHeight = 16.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.Bold
            ),
            decorationBox = { content ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    this@Row.AnimatedVisibility(
                        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                        visible = keyword().isEmpty()
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 2.dp),
                            text = "输入关键词以匹配元素",
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground.copy(0.3f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            content()
                        }

                        AnimatedVisibility(
                            enter = fadeIn() + scaleIn(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                ),
                                initialScale = 0f
                            ),
                            exit = fadeOut() + scaleOut(
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                targetScale = 0f
                            ),
                            visible = keyword().isNotEmpty()
                        ) {
                            IconButton(
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                                onClick = { onUpdateKeyword("") }
                            ) {
                                Icon(
                                    imageVector = RemixIcon.System.closeLine,
                                    contentDescription = "clear"
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
