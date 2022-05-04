package com.lalilu.lmusic.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.utils.fetcher.getCoverFromMediaItem

@Composable
fun SongDetailScreen(
    mediaItem: MediaItem,
    navigateTo: (destination: String) -> Unit = {}
) {
    val imagePainter = rememberImagePainter(
        data = mediaItem.getCoverFromMediaItem()
    ) {
        size(SizeUtils.dp2px(128f))
    }
    val title = mediaItem.mediaMetadata.title?.toString()
        ?: stringResource(id = MainScreenData.SongDetail.title)
    val subTitle = mediaItem.mediaMetadata.artist?.toString()
        ?: stringResource(id = MainScreenData.SongDetail.subTitle)

    SongDetailScreen(
        title = title,
        subTitle = subTitle,
        imagePainter = imagePainter,
        onSearchForLyric = {
            navigateTo("${MainScreenData.SearchForLyric.name}/${mediaItem.mediaId}")
        },
        onAddSongToPlaylist = {
            navigateTo("${MainScreenData.AddToPlaylist.name}/${mediaItem.mediaId}")
        },
        onSetSongToNext = {

        }
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SongDetailScreen(
    title: String,
    subTitle: String,
    imagePainter: ImagePainter,
    onSetSongToNext: () -> Unit = {},
    onAddSongToPlaylist: () -> Unit = {},
    onSearchForLyric: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeader(title = title, subTitle = subTitle) {
            Surface(
                elevation = 4.dp,
                shape = RoundedCornerShape(2.dp)
            ) {
                if (imagePainter.state.painter != null) {
                    Image(
                        painter = imagePainter, contentDescription = "CoverImage",
                        modifier = Modifier
                            .sizeIn(
                                minHeight = 64.dp,
                                maxHeight = 128.dp,
                                minWidth = 64.dp,
                                maxWidth = 144.dp
                            )
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_line),
                        contentDescription = "",
                        contentScale = FixedScale(2.5f),
                        colorFilter = ColorFilter.tint(color = Color.LightGray),
                        modifier = Modifier
                            .size(128.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            SongDetailActionButton(
                textRes = R.string.button_set_song_to_next,
                onClick = onSetSongToNext
            )
            SongDetailActionButton(
                textRes = R.string.button_add_song_to_playlist,
                onClick = onAddSongToPlaylist
            )
            SongDetailActionButton(
                textRes = R.string.button_search_for_lyric,
                onClick = onSearchForLyric
            )
        }
    }
}

@Composable
fun SongDetailActionButton(
    @StringRes textRes: Int,
    onClick: () -> Unit = {},
) = SongDetailActionButton(
    text = stringResource(id = textRes),
    onClick = onClick
)

@Composable
fun SongDetailActionButton(
    text: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = Color(0x25006E7C),
            contentColor = Color(0xFF006E7C)
        )
    ) {
        Text(text = text)
    }
}

@Composable
fun EmptySongDetailScreen() {
    Text(text = "无法获取该歌曲信息", modifier = Modifier.padding(20.dp))
}