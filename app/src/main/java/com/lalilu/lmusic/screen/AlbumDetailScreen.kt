package com.lalilu.lmusic.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import coil.compose.rememberImagePainter
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SongCard
import com.lalilu.lmusic.screen.viewmodel.MediaBrowserViewModel

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AlbumDetailScreen(
    album: MediaItem,
    songs: List<MediaItem>,
    contentPaddingForFooter: Dp = 0.dp,
    navigateTo: (destination: String) -> Unit = {},
    mediaBrowserViewModel: MediaBrowserViewModel = hiltViewModel()
) {
    val haptic = LocalHapticFeedback.current
    val sortedItems = remember { songs.toMutableStateList() }
    val title = album.mediaMetadata.albumTitle?.toString()
        ?: stringResource(id = MainScreenData.AlbumDetail.title)
    val subTitle = album.mediaMetadata.albumArtist?.toString()
        ?: stringResource(id = MainScreenData.AlbumDetail.subTitle)

    val imagePainter = rememberImagePainter(
        data = album.mediaMetadata.artworkUri
    ) {
        placeholder(R.drawable.item_cover_bg)
        size(SizeUtils.dp2px(128f))
    }

    val onSongSelected: (Int) -> Unit = remember {
        { index: Int ->
            mediaBrowserViewModel.playSongWithPlaylist(
                items = songs,
                index = index
            )
        }
    }

    val onSongShowDetail: (String) -> Unit = remember {
        { mediaId ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            navigateTo("${MainScreenData.SongDetail.name}/$mediaId")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigatorHeader(title = title, subTitle = subTitle) {
            Surface(
                elevation = 4.dp,
                shape = RoundedCornerShape(2.dp)
            ) {
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
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = contentPaddingForFooter
            )
        ) {
            itemsIndexed(items = sortedItems) { index, item ->
                SongCard(
                    modifier = Modifier.animateItemPlacement(),
                    index = index,
                    mediaItem = item,
                    onSongSelected = onSongSelected,
                    onSongShowDetail = onSongShowDetail
                )
            }
        }
    }
}

@Composable
fun EmptyAlbumDetailScreen() {
    Text(text = "无法获取该专辑信息", modifier = Modifier.padding(20.dp))
}