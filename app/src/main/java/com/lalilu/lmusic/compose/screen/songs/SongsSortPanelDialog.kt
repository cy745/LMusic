package com.lalilu.lmusic.compose.screen.songs

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmusic.LMusicTheme


@Composable
internal fun SongsSortPanelDialog(
    isVisible: MutableState<Boolean>,
    supportSortActions: Set<ListAction>,
    isSortActionSelected: (ListAction) -> Boolean = { false },
    onSelectSortAction: (ListAction) -> Unit
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SongsSortPanelDialogContent(
                supportSortActions = supportSortActions,
                isSortActionSelected = isSortActionSelected,
                onSelectSortAction = onSelectSortAction,
                onDismiss = { dismiss() }
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
    isSortActionSelected: (ListAction) -> Boolean = { false },
    onSelectSortAction: (ListAction) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val colors = ChipDefaults.filterChipColors(
        selectedBackgroundColor = Color(0xFF029DF3),
        selectedContentColor = Color.White,
        backgroundColor = MaterialTheme.colors.onSurface
            .compositeOver(MaterialTheme.colors.surface)
            .copy(alpha = 0.05f)
    )

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
        VerticalGrid(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            columns = SimpleGridCells.Fixed(2)
        ) {
            Row(
                modifier = Modifier
                    .span(2)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = "常用排序逻辑",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = { onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }

            supportSortActions.forEach {
                FilterChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = colors,
                    shape = RoundedCornerShape(5.dp),
                    selected = isSortActionSelected(it),
                    onClick = { onSelectSortAction(it) },
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            text = stringResource(id = it.titleRes)
                        )

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "test",
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                        )
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