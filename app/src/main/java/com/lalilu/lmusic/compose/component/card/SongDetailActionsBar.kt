package com.lalilu.lmusic.compose.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.compose.component.base.IconCheckButton
import com.lalilu.lmusic.compose.component.base.IconTextButton

@Composable
fun SongDetailActionsBar(
    getIsLiked: () -> Boolean = { false },
    onIsLikedChange: (Boolean) -> Unit = {},
    onPlaySong: () -> Unit = {},
    onSetSongToNext: () -> Unit = {},
    onAddSongToPlaylist: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        IconTextButton(
            text = stringResource(id = R.string.text_button_play),
            color = Color(0xFF006E7C),
            onClick = onPlaySong
        )
        IconTextButton(
            text = stringResource(id = R.string.button_set_song_to_next),
            color = Color(0xFF006E7C),
            onClick = onSetSongToNext
        )
        IconTextButton(
            color = Color(0xFF006E7C),
            text = stringResource(id = R.string.button_add_song_to_playlist),
            iconPainter = painterResource(id = R.drawable.ic_play_list_add_line),
            onClick = onAddSongToPlaylist
        )
        IconCheckButton(
            getIsChecked = getIsLiked,
            onCheckedChange = onIsLikedChange,
            checkedColor = MaterialTheme.colors.primary,
            checkedIconRes = R.drawable.ic_heart_3_fill,
            normalIconRes = R.drawable.ic_heart_3_line
        )
    }
}