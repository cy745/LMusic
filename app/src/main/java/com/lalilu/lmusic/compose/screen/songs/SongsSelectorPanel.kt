package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lalilu.RemixIcon
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.base.screen.ScreenBarFactory
import com.lalilu.component.navigation.NavigateCommonBarContent
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.closeLine


@Composable
internal fun ScreenBarFactory.SongsSelectorPanel(
    isVisible: MutableState<Boolean>,
    screenActions: List<ScreenAction>? = null,
) {
    RegisterContent(isVisible = isVisible, onBackPressed = { }) {
        SongsSelectorPanelContent(
            modifier = Modifier,
            screenActions = screenActions,
            onBackPress = { isVisible.value = false }
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
