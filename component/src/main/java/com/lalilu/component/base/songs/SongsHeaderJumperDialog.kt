package com.lalilu.component.base.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.lmedia.extension.GroupIdentity

@Composable
fun SongsHeaderJumperDialog(
    isVisible: () -> Boolean,
    onDismiss: () -> Unit,
    items: () -> Collection<GroupIdentity>,
    onSelectItem: (item: GroupIdentity) -> Unit = {}
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SongsHeaderJumperDialogContent(
                items = items,
                onSelectItem = onSelectItem
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
    items: () -> Collection<GroupIdentity>,
    onSelectItem: (item: GroupIdentity) -> Unit = {}
) {
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    val charMapping = remember {
        items().filter { it.text.isNotBlank() }
            .groupBy { it.text[0].category }
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        columns = GridCells.Adaptive(56.dp),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = navigationBarsPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        charMapping.forEach { (key, value) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = Color.White,
                    text = key.name
                    // TODO 需要为CharCategory设置i18n转换
                )
            }

            items(items = value) {
                Chip(
                    modifier = Modifier,
                    onClick = { onSelectItem(it) }
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
}