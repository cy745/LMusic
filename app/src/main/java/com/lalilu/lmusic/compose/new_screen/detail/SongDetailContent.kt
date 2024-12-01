package com.lalilu.lmusic.compose.new_screen.detail

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.lalilu.common.base.SourceType
import com.lalilu.component.base.LocalSmartBarPadding
import com.lalilu.lmedia.entity.FileInfo
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.Metadata

private val lSong = LSong(
    id = "inceptos",
    metadata = Metadata(
        title = "maluisset",
        album = "honestatis",
        artist = "persius",
        albumArtist = "simul",
        composer = "eum",
        lyricist = "eos",
        comment = "morbi",
        genre = "dolore",
        track = "oratio",
        disc = "sapien",
        date = "iudicabit",
        duration = 5920,
        dateAdded = 2540,
        dateModified = 3267
    ), fileInfo = FileInfo(
        mimeType = "molestiae",
        directoryPath = "amet",
        pathStr = null,
        fileName = null,
        size = 5613
    ), uri = Uri.EMPTY,
    sourceType = SourceType.Local, albumId = null
)

@Composable
fun SongDetailContent(
    modifier: Modifier = Modifier,
    song: LSong = lSong,
    progress: Float = 0f,
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(bottom = LocalSmartBarPadding.current.value.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier,
            elevation = 2.dp,
            shape = RoundedCornerShape(10.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song)
                    .size(width = 1024, height = 1024)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.FillWidth,
                contentDescription = ""
            )
        }

        Text(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = 1f + (0.1f * progress)
                    scaleY = scaleX

                    transformOrigin = TransformOrigin(0f, 0f)
                },
            text = song.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 16.sp
        )

        SongArtistsRow(
            modifier = Modifier.fillMaxWidth(),
            artists = song.artists
        )

        song.album?.let {
            SongAlbumInfoCard(
                modifier = Modifier.fillMaxWidth(),
                album = it
            )
        }

        SongActionsCard(
            modifier = Modifier.fillMaxWidth(),
            song = song
        )

        SongInformationCard(
            modifier = Modifier.fillMaxWidth(),
            song = song
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun SongDetailContentPreview() {
    val expended = remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (expended.value) 1f else 0f,
        label = "progress"
    )

    SongDetailContent(
        progress = progress,
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun SongDetailContentPreview2() {
    val expended = remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (expended.value) 0f else 1f,
        label = "progressReverse"
    )

    SongDetailContent(
        progress = progress,
    )
}