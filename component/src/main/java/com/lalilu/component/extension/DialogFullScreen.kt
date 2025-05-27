package com.lalilu.component.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

@Immutable
data class CustomDialogProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val isAppearanceLightNavigationBars: Boolean = true,
    val direction: DirectionState,
    val backgroundDimEnabled: Boolean = true,
    val backgroundDimPercent: Float = 0.5f,
    val statusBarColor: Color = Color.Transparent,
    val navBarColor: Color = Color.Transparent,
    val durationMillis: Int = DefaultDurationMillis,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
)

enum class DirectionState {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM
}

@Composable
private fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
private fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

private tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}

@Composable
fun DialogFullScreen(
    isActiveClose: Boolean,
    onDismissRequest: () -> Unit,
    properties: CustomDialogProperties,
    content: @Composable () -> Unit
) {
    var isAnimateLayout by remember { mutableStateOf(false) }
    var isBackPress by remember { mutableStateOf(false) }

    LaunchedEffect(isActiveClose) {
        if (isActiveClose) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    val handleBackPress = {
        if (!isBackPress) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            // 这里不使用新的测量规范，不能设置为false
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy
        ),
        content = {
            val animColor = remember { Animatable(Color.Transparent) }
            LaunchedEffect(isAnimateLayout, properties.backgroundDimPercent) {
                if (properties.backgroundDimEnabled) {
                    animColor.animateTo(
                        if (isAnimateLayout) Color.Black.copy(alpha = properties.backgroundDimPercent) else Color.Transparent,
                        animationSpec = tween(properties.durationMillis)
                    )
                } else {
                    delay(properties.durationMillis.toLong())
                }
                if (!isAnimateLayout) {
                    onDismissRequest.invoke()
                }
            }
            val activityWindow = getActivityWindow()
            val dialogWindow = getDialogWindow()
            val parentView = LocalView.current.parent as View
            SideEffect {
                if (activityWindow == null || dialogWindow == null || isBackPress || isAnimateLayout)
                    return@SideEffect

                // 禁止Dialog跟随软键盘高度变化，应使用Compose提供的imePadding替代
                dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

                val attributes = WindowManager.LayoutParams()
                attributes.copyFrom(activityWindow.attributes)
                attributes.type = dialogWindow.attributes.type
                dialogWindow.attributes = attributes
                // 修复Android10 - Android11出现背景全黑的情况
                dialogWindow.setBackgroundDrawableResource(android.R.color.transparent)

                dialogWindow.setLayout(
                    activityWindow.decorView.width,
                    activityWindow.decorView.height
                )
                // 修复Android低版本系统，状态栏和导航栏颜色问题
                dialogWindow.statusBarColor = properties.statusBarColor.toArgb()
                dialogWindow.navigationBarColor = properties.navBarColor.toArgb()

                WindowCompat.getInsetsController(dialogWindow, parentView)
                    .isAppearanceLightNavigationBars =
                    properties.isAppearanceLightNavigationBars
                isAnimateLayout = true
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when (properties.direction) {
                    DirectionState.TOP -> Alignment.TopCenter
                    DirectionState.LEFT -> Alignment.CenterStart
                    DirectionState.RIGHT -> Alignment.CenterEnd
                    else -> Alignment.BottomCenter
                }
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animColor.value)
                        .clickOutSideModifier(
                            dismissOnClickOutside = properties.dismissOnClickOutside,
                            onTap = handleBackPress
                        )
                )
                AnimatedVisibility(
                    modifier = Modifier.pointerInput(Unit) {},
                    visible = isAnimateLayout,
                    enter = when (properties.direction) {
                        DirectionState.TOP -> slideInVertically(initialOffsetY = { -it })
                        DirectionState.LEFT -> slideInHorizontally(initialOffsetX = { -it })
                        DirectionState.RIGHT -> slideInHorizontally(initialOffsetX = { it })
                        else -> slideInVertically(initialOffsetY = { it })
                    },
                    exit = when (properties.direction) {
                        DirectionState.TOP -> fadeOut() + slideOutVertically(targetOffsetY = { -it })
                        DirectionState.LEFT -> fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        DirectionState.RIGHT -> fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                        else -> fadeOut() + slideOutVertically(targetOffsetY = { it })
                    }
                ) {
                    content()
                }
            }
            BackHandler(enabled = properties.dismissOnBackPress, onBack = handleBackPress)
        }
    )
}

private fun Modifier.clickOutSideModifier(
    dismissOnClickOutside: Boolean,
    onTap: () -> Unit
) = this.then(
    if (dismissOnClickOutside) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                onTap()
            })
        }
    } else Modifier
)