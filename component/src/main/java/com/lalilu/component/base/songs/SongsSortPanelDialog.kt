package com.lalilu.component.base.songs

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SelectableChipColors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.extension.SortRuleNormal
import com.lalilu.lmedia.extension.sortable.SortAction
import com.lalilu.lmedia.extension.sortable.SortConfig


@Composable
fun SongsSortPanelDialog(
    isVisible: () -> Boolean,
    onDismiss: () -> Unit,
    supportSortActions: Collection<SortAction>,
    sortConfig: () -> SortConfig,
    onUpdateSortConfig: (SortConfig) -> Unit,
    selectedSortAction: () -> SortAction?,
    onSelectSortAction: (SortAction) -> Unit
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SongsSortPanelDialogContent(
                supportSortActions = supportSortActions,
                selectedSortAction = selectedSortAction,
                onSelectSortAction = onSelectSortAction,
                sortConfig = sortConfig,
                onUpdateSortConfig = onUpdateSortConfig,
                onDismiss = { dismiss() }
            )
        }
    }

    DialogWrapper.register(
        isVisible = isVisible,
        onDismiss = onDismiss,
        dialogItem = dialog
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SongsSortPanelDialogContent(
    modifier: Modifier = Modifier,
    supportSortActions: Collection<SortAction>,
    selectedSortAction: () -> SortAction? = { null },
    onSelectSortAction: (SortAction) -> Unit = {},
    sortConfig: () -> SortConfig = { SortConfig() },
    onUpdateSortConfig: (SortConfig) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val colors = ChipDefaults.filterChipColors(
        selectedBackgroundColor = Color(0xFF029DF3),
        selectedContentColor = Color.Black,
        contentColor = MaterialTheme.colors.onBackground,
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colors.background,
        elevation = 10.dp
    ) {
        VerticalGrid(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            columns = SimpleGridCells.Fixed(6)
        ) {
            val needSpanIndex = remember(supportSortActions) {
                if (supportSortActions.size % 2 == 1) supportSortActions.indices.last else -1
            }

            supportSortActions.forEachIndexed { index, sortAction ->
                val info = sortAction.getActionInfo()

                SortItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .run { if (needSpanIndex == index) this.span(6) else this.span(3) },
                    title = info.title,
                    subTitle = info.subTitle ?: "",
                    colors = colors,
                    selected = { selectedSortAction() == sortAction },
                    onClick = { onSelectSortAction(sortAction) }
                )
            }

            Spacer(
                modifier = Modifier
                    .span(6)
                    .padding(vertical = 4.dp)
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.onBackground.copy(0.05f))
            )

            SortItem(
                modifier = Modifier.span(2),
                title = "取消",
                center = true,
                selected = { true },
                onClick = { onDismiss() },
                colors = ChipDefaults.filterChipColors(
                    selectedBackgroundColor = Color(0x52EF0606),
                    selectedContentColor = Color(0xFFEF0606),
                ),
            )
            SortItem(
                modifier = Modifier.span(2),
                title = "隐藏分组",
                center = true,
                selected = { sortConfig().hideGroup },
                onClick = { onUpdateSortConfig(sortConfig().let { it.copy(hideGroup = !it.hideGroup) }) },
                colors = ChipDefaults.filterChipColors(
                    selectedBackgroundColor = Color(0x523F51B5),
                    selectedContentColor = Color(0xFF3F51B5),
                    contentColor = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialTheme.colors.onSurface
                        .compositeOver(MaterialTheme.colors.surface)
                        .copy(alpha = 0.05f)
                ),
            )
            SortItem(
                modifier = Modifier.span(2),
                title = "顺序倒转",
                center = true,
                selected = { sortConfig().reverse },
                onClick = { onUpdateSortConfig(sortConfig().let { it.copy(reverse = !it.reverse) }) },
                colors = ChipDefaults.filterChipColors(
                    disabledBackgroundColor = Color.LightGray,
                    selectedBackgroundColor = Color(0x526A10F5),
                    selectedContentColor = Color(0xFF6A10F5),
                    contentColor = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialTheme.colors.onSurface
                        .compositeOver(MaterialTheme.colors.surface)
                        .copy(alpha = 0.05f)
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SortItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    subTitle: String? = "",
    center: Boolean = false,
    colors: SelectableChipColors = ChipDefaults.filterChipColors(),
    selected: () -> Boolean,
    onClick: () -> Unit = {}
) {
    FilterChip(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        colors = colors,
        enabled = enabled,
        shape = RoundedCornerShape(5.dp),
        selected = selected(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                text = title,
                textAlign = if (center) TextAlign.Center else TextAlign.Start,
                color = LocalContentColor.current
            )

            subTitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = LocalContentColor.current.copy(0.5f)
                )
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
    SongsSortPanelDialogContent(
        supportSortActions = setOf(SortRuleNormal())
    )
}

@Preview(
    showSystemUi = false,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun SongsSortPanelDialogPV() {
    SongsSortPanelDialogContent(
        supportSortActions = setOf(SortRuleNormal())
    )
}