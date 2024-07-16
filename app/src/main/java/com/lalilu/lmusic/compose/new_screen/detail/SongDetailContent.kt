package com.lalilu.lmusic.compose.new_screen.detail

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.lalilu.common.base.SourceType
import com.lalilu.component.base.LocalPaddingValue
import com.lalilu.lmedia.entity.FileInfo
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.Metadata

private val lSong = LSong(
    id = "inceptos", name = "Kim Serrano", metadata = Metadata(
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
    val paddingTop = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()

    val scene = remember {
        MotionScene {
            val coverRef = createRefFor("cover")
            val titleRow = createRefFor("title")
            val subTitleRow = createRefFor("subTitle")
            val content = createRefFor("content")

            val collapsed = constraintSet {
                constrain(coverRef) {
                    top.linkTo(parent.top, 16.dp)
                    start.linkTo(parent.start, 16.dp)

                    width = Dimension.value(72.dp)
                    height = Dimension.value(72.dp)
                }

                constrain(titleRow) {
                    top.linkTo(coverRef.top)
                    start.linkTo(coverRef.end, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

                constrain(subTitleRow) {
                    top.linkTo(titleRow.bottom, 8.dp)
                    start.linkTo(coverRef.end, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

                val barrier = createBottomBarrier(coverRef, subTitleRow)

                constrain(content) {
                    top.linkTo(barrier, 16.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
            }
            val expended = constraintSet {
                constrain(coverRef) {
                    top.linkTo(parent.top, 16.dp + paddingTop + 24.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.preferredWrapContent
                }

                constrain(titleRow) {
                    top.linkTo(coverRef.bottom, 16.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.preferredWrapContent
                }

                constrain(subTitleRow) {
                    top.linkTo(titleRow.bottom, 8.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }

                val barrier = createBottomBarrier(coverRef, subTitleRow)

                constrain(content) {
                    top.linkTo(barrier, 16.dp)
                    start.linkTo(parent.start, 16.dp)
                    end.linkTo(parent.end, 16.dp)

                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
            }

            defaultTransition(from = collapsed, to = expended)
        }
    }

    MotionLayout(
        modifier = modifier.fillMaxSize(),
        motionScene = scene,
        progress = progress
    ) {
        Surface(
            modifier = Modifier.layoutId("cover"),
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
                .layoutId("title")
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

        Text(
            modifier = Modifier.layoutId("subTitle"),
            text = song.subTitle,
            color = MaterialTheme.colors.onBackground.copy(0.6f),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 12.sp
        )

        Column(
            modifier = Modifier
                .layoutId("content")
                .padding(bottom = LocalPaddingValue.current.value.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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