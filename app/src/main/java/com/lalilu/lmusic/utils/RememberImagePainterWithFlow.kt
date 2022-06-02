package com.lalilu.lmusic.utils

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.lalilu.lmusic.datasource.extensions.toUri
import com.lalilu.lmusic.utils.fetcher.getCoverFromMediaItem
import com.lalilu.lmusic.viewmodel.NetworkDataViewModel

/**
 * 监听Flow进行更新
 */
@Composable
fun rememberCoverWithFlow(
    mediaItem: MediaItem,
    networkDataViewModel: NetworkDataViewModel = hiltViewModel(),
    builder: ImageRequest.Builder.() -> Unit = {}
): ImagePainter {
    val data: Any = networkDataViewModel.getNetworkDataFlowByMediaId(mediaItem.mediaId)
        .collectAsState(initial = null).value?.cover?.toUri()?.takeIf {
            it.scheme == "http" || it.scheme == "https"
        } ?: mediaItem.getCoverFromMediaItem()

    return rememberImagePainter(
        data = data,
        builder = builder
    )
}

/**
 * 单次查询
 */
@Composable
fun rememberCoverForOnce(
    mediaItem: MediaItem,
    networkDataViewModel: NetworkDataViewModel = hiltViewModel(),
    builder: ImageRequest.Builder.() -> Unit = {}
): ImagePainter {
    var data by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(mediaItem) {
        data = networkDataViewModel.getNetworkDataByMediaId(mediaItem.mediaId)
            ?.cover?.toUri()?.takeIf {
                it.scheme == "http" || it.scheme == "https"
            } ?: mediaItem.getCoverFromMediaItem()
    }

    return rememberImagePainter(
        data = data,
        builder = builder
    )
}