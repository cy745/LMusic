package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper


@Composable
internal fun SongsHeaderJumperDialog(
    isVisible: MutableState<Boolean>,
) {
    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SongsHeaderJumperDialogContent(

            )
        }
    }

    DialogWrapper.register(
        isVisible = isVisible,
        dialogItem = dialog
    )
}

@Composable
private fun SongsHeaderJumperDialogContent(modifier: Modifier = Modifier) {

}