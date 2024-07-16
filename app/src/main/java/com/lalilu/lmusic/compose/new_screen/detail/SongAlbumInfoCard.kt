package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.lalbum.screen.AlbumDetailScreen
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmusic.compose.component.card.RecommendCardCover
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongAlbumInfoCard(
    modifier: Modifier = Modifier,
    album: LAlbum,
) {
    val navigator: GlobalNavigator = koinInject()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        onClick = { navigator.navigateTo(AlbumDetailScreen(albumId = album.id)) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RecommendCardCover(
                width = { 125.dp },
                height = { 125.dp },
                imageData = { album }
            )
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.subtitle1,
                    color = dayNightTextColor()
                )
                album.artistName?.let { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.subtitle2,
                        color = dayNightTextColor(0.5f)
                    )
                }
            }
        }
    }
}