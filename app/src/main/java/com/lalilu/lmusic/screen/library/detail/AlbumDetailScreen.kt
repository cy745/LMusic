package com.lalilu.lmusic.screen.library.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.items
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.NavigatorFooter
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.viewmodel.MainViewModel

@Composable
fun AlbumDetailScreen(
    album: LAlbum,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val songs = album.songs
    val haptic = LocalHapticFeedback.current
    val windowSize = LocalWindowSize.current
    val navController = LocalNavigatorHost.current
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp
    val sortedItems = remember { songs.toMutableStateList() }
    val title = album.name
    val subTitle = album.name // todo AlbumArtist 补足
        ?: stringResource(id = MainScreenData.AlbumsDetail.subTitle)

    val onSongSelected: (Int) -> Unit = remember {
        { index: Int ->
            mainViewModel.playSongWithPlaylist(
                items = songs.items(),
                index = index
            )
        }
    }

    val onSongShowDetail: (String) -> Unit = remember {
        { mediaId ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            navController.navigate("${MainScreenData.SongsDetail.name}/$mediaId")
        }
    }

    SmartBar.RestoreOnDispose()

    LaunchedEffect(Unit) {
        SmartBar.setMainBar { NavigatorFooter(navController) }
            .setExtraBar(item = null)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = contentPaddingForFooter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .crossfade(true)
                            .data(album)
                            .build(),
                        contentDescription = ""
                    ) {
                        val state = painter.state
                        if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_music_2_line),
                                contentDescription = "",
                                contentScale = FixedScale(2.5f),
                                colorFilter = ColorFilter.tint(color = Color.LightGray),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        } else {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth,
                                contentDescription = "CoverImage"
                            )
                        }
                    }
                }
            }
        }

        item {
            NavigatorHeader(title = title, subTitle = subTitle)
        }

        itemsIndexed(sortedItems) { index, item ->
            SongCard(
                index = index,
                song = item,
                onSongSelected = onSongSelected,
                onSongShowDetail = onSongShowDetail
            )
        }
    }
}

@Composable
fun EmptyAlbumDetailScreen() {
    Text(text = "无法获取该专辑信息", modifier = Modifier.padding(20.dp))
}