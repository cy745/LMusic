package com.lalilu.component.smartbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.screen.ActionContext
import com.lalilu.component.base.screen.ScreenAction
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.smartbar.component.ActionItem
import kotlin.collections.forEach


@Composable
internal fun MoreActionPanelDialog(
    isVisible: MutableState<Boolean>,
    actions: List<ScreenAction>,
) {
    val actualActions = rememberUpdatedState(newValue = actions)

    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            MoreActionPanelDialogContent(
                actions = actualActions.value,
                onDismiss = { dismiss() }
            )
        }
    }

    DialogWrapper.register(
        isVisible = { isVisible.value },
        onDismiss = { isVisible.value = false },
        dialogItem = dialog
    )
}

@Composable
private fun MoreActionPanelDialogContent(
    modifier: Modifier = Modifier,
    actions: List<ScreenAction>,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .navigationBarsPadding(),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(0.1f)),
        shape = RoundedCornerShape(18.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            actions.forEach { action ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    ActionItem(
                        action = action,
                        actionContext = ActionContext(isFullyExpanded = true)
                    )
                }
            }
        }
    }
}
