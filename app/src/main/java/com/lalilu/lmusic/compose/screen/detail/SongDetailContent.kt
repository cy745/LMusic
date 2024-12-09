package com.lalilu.lmusic.compose.screen.detail

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.lalilu.lmusic.compose.new_screen.detail.SongActionsCard
import com.lalilu.lmusic.compose.new_screen.detail.SongAlbumInfoCard
import com.lalilu.lmusic.compose.new_screen.detail.SongArtistsRow
import com.lalilu.lmusic.compose.new_screen.detail.SongInformationCard


@Composable
fun SongDetailContent(
    modifier: Modifier = Modifier,
    song: () -> LSong? = { lSong },
    progress: Float = 0f,
) {
    val bottomPadding = LocalSmartBarPadding.current.value
    val navigationBar = WindowInsets.navigationBars.asPaddingValues()

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = navigationBar.calculateBottomPadding()
                    + bottomPadding.calculateBottomPadding()
                    + 16.dp
        )
    ) {
        item(key = "MAIN_COVER") {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .height(300.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song())
                    .size(width = 1024, height = 1024)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.FillWidth,
                contentDescription = ""
            )
        }

        song()?.let { song ->
            item(key = "TITLE") {
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
            }

            item(key = "ARTISTS") {
                SongArtistsRow(
                    modifier = Modifier.fillMaxWidth(),
                    artists = song.artists
                )
            }

            item(key = "ALBUM") {
                song.album?.let {
                    SongAlbumInfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        album = it
                    )
                }
            }

            item(key = "ACTIONS") {
                SongActionsCard(
                    modifier = Modifier.fillMaxWidth(),
                    song = song
                )
            }

            item(key = "INFOS") {
                SongInformationCard(
                    modifier = Modifier.fillMaxWidth(),
                    song = song
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun SongDetailContentPreview() {
    SongDetailContent()
}

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