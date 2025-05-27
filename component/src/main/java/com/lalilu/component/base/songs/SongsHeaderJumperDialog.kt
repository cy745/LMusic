package com.lalilu.component.base.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.RemixIcon
import com.lalilu.component.extension.DEFAULT_DIALOG_PROPERTIES
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.extension.sortable.GroupId
import com.lalilu.lmedia.extension.sortable.SortResult
import com.lalilu.lmedia.extension.sortable.Sortable
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.closeLine

@Composable
fun <T : Sortable> SongsHeaderJumperDialog(
    isVisible: () -> Boolean,
    onDismiss: () -> Unit,
    sortResult: SortResult<T>,
    onSelectItem: (item: GroupId) -> Unit = {}
) {
    val items = rememberUpdatedState(sortResult)

    val dialog = remember {
        DialogItem.Dynamic(
            backgroundColor = Color.Transparent,
            properties = DEFAULT_DIALOG_PROPERTIES.copy(backgroundDimPercent = 0.8f)
        ) {
            SongsHeaderJumperDialogContent(
                items = { items.value.groups.mapNotNull { it.groupId } },
                onDismiss = ::dismiss,
                onSelectItem = {
                    onSelectItem(it)
                    dismiss()
                }
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
private fun SongsHeaderJumperDialogContent(
    modifier: Modifier = Modifier,
    items: () -> Collection<GroupId>,
    onSelectItem: (item: GroupId) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val charMapping = remember(items()) {
        items().filter { it.text.isNotBlank() }
            .groupBy { it.text[0].category }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalFadingEdges(
                    gravity = FadingEdgesGravity.Start,
                    length = 100.dp,
                    fillType = FadingEdgesFillType.FadeClip()
                )
                .verticalFadingEdges(
                    gravity = FadingEdgesGravity.End,
                    length = 24.dp,
                    fillType = FadingEdgesFillType.FadeClip()
                ),
            columns = GridCells.Fixed(12),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 200.dp,
                bottom = navigationBarsPadding.calculateBottomPadding() + 32.dp + 64.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            charMapping.forEach { (key, value) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = Color.White,
                        text = i18nForCharCategory(key)
                    )
                }

                items(
                    items = value,
                    key = { it },
                    span = { GridItemSpan(maxLineSpan / 6) }
                ) {
                    Chip(
                        modifier = Modifier.aspectRatio(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
                        ),
                        onClick = { onSelectItem(it) },
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.h6,
                            text = it.text
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .size(56.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = RemixIcon.System.closeLine,
                tint = Color.White,
                contentDescription = null
            )
        }
    }
}

@Stable
@Composable
private fun i18nForCharCategory(category: CharCategory): String {
    // TODO 待完善多语言
    return when (category) {
        CharCategory.MATH_SYMBOL -> "数学符号"
        CharCategory.CURRENCY_SYMBOL -> "货币符号"
        CharCategory.DECIMAL_DIGIT_NUMBER -> "数字"
        CharCategory.LOWERCASE_LETTER -> "小写字母"
        CharCategory.UPPERCASE_LETTER -> "大写字母"
        CharCategory.TITLECASE_LETTER -> "标题字母"
        CharCategory.MODIFIER_LETTER -> "修饰字母"
        else -> "其他符号"
    }
}