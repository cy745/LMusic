package com.lalilu.lmusic.compose.screen.songs

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmusic.LMusicTheme


@Composable
internal fun SongsSortPanelDialog(
    isVisible: MutableState<Boolean>,
    supportSortActions: Set<ListAction>,
    onSelectSortAction: (ListAction) -> Unit
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SongsSortPanelDialogContent(
                supportSortActions = supportSortActions,
                onSelectSortAction = onSelectSortAction
            )
        }
    }

    DialogWrapper.register(
        isVisible = isVisible,
        dialogItem = dialog
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SongsSortPanelDialogContent(
    modifier: Modifier = Modifier,
    supportSortActions: Set<ListAction>,
    onSelectSortAction: (ListAction) -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(0.1f)),
        shape = RoundedCornerShape(18.dp),
        elevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            supportSortActions.forEach {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color(0xFF6176F8),
                    onClick = { onSelectSortAction(it) }
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text(text = stringResource(id = it.titleRes))
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = false,
    showBackground = true,
)
@Composable
private fun SongsSortPanelDialogPVDay() {
    LMusicTheme {
        SongsSortPanelDialogContent(
            supportSortActions = setOf(SortStaticAction.Normal)
        )
    }
}

@Preview(
    showSystemUi = false,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun SongsSortPanelDialogPV() {
    LMusicTheme {
        SongsSortPanelDialogContent(
            supportSortActions = setOf(SortStaticAction.Normal)
        )
    }
}