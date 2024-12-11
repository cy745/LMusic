package com.lalilu.lmusic.compose.new_screen.detail

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.lalilu.R
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.entity.LAlbum

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongAlbumInfoCard(
    modifier: Modifier = Modifier,
    album: LAlbum,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = 1.dp,
        onClick = {
            AppRouter.route("/pages/albums/detail")
                .with("albumId", album.id)
                .push()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // TODO Animation BG

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.onBackground.copy(0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album)
                        .placeholder(R.drawable.ic_music_2_line_100dp)
                        .error(R.drawable.ic_music_2_line_100dp)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Recommend Card Cover Image"
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
}

@Composable
fun GridAnimation(
    modifier: Modifier = Modifier,
    images: List<String> = emptyList()
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .aspectRatio(1f)
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                spacing = MarqueeSpacing(8.dp),
                velocity = 30.dp,
                repeatDelayMillis = 0,
                initialDelayMillis = 0
            )
    ) {
        VerticalGrid(
            modifier = Modifier.size(36.dp * 3f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            columns = SimpleGridCells.Fixed(3)
        ) {
            images.forEach { song ->
                AsyncImage(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.onBackground.copy(0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_music_2_line_100dp)
                        .error(R.drawable.ic_music_2_line_100dp)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Recommend Card Cover Image"
                )
            }
        }
    }
}