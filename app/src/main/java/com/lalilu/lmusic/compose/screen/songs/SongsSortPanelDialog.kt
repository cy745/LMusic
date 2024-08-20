package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper

@Composable
internal fun SongsSortPanelDialog(
    songsSM: SongsSM
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {

        }
    }

    DialogWrapper.register(
        isVisible = songsSM.showSortPanel,
        dialogItem = dialog
    )
}