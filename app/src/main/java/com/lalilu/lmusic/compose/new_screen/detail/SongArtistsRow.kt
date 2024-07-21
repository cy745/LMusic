package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lartist.screen.ArtistDetailScreen
import com.lalilu.lmedia.entity.LArtist

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun SongArtistsRow(
    modifier: Modifier = Modifier,
    artists: Set<LArtist>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        artists.forEach {
            Chip(
                onClick = {
                    AppRouter.intent(
                        NavIntent.Push(ArtistDetailScreen(artistName = it.name))
                    )
                },
                colors = ChipDefaults.outlinedChipColors(),
            ) {
                Text(
                    text = it.name,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                        .copy(alpha = 0.7f)
                )
            }
        }
    }
}