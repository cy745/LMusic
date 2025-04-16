package com.lalilu.component.base.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.smartbar.NavigateCommonBarContent
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.closeLine


@Composable
fun ScreenBarFactory.SongsSelectorPanel(
    isVisible: () -> Boolean,
    onDismiss: () -> Unit,
    screenActions: List<ScreenAction>? = null,
) {
    RegisterContent(
        isVisible = isVisible,
        onDismiss = onDismiss,
        onBackPressed = { }
    ) {
        SongsSelectorPanelContent(
            modifier = Modifier,
            screenActions = screenActions,
            onBackPress = { onDismiss() }
        )
    }
}

@Composable
private fun SongsSelectorPanelContent(
    modifier: Modifier = Modifier,
    screenActions: List<ScreenAction>?,
    onBackPress: (() -> Unit)? = null
) {
    val dialogVisible = remember { mutableStateOf(false) }

    NavigateCommonBarContent(
        modifier = modifier,
        previousTitle = "取消",
        previousIcon = RemixIcon.System.closeLine,
        dialogVisible = dialogVisible,
        screenActions = screenActions,
        actionContext = ActionContext(false),
        onBackPress = onBackPress
    )
}
