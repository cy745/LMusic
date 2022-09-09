package com.lalilu.lmusic.utils

//import androidx.compose.runtime.*
//import androidx.hilt.navigation.compose.hiltViewModel
//import coil.compose.ImagePainter
//import coil.compose.rememberImagePainter
//import coil.request.ImageRequest
//import com.lalilu.lmedia.indexer.Library
//import com.lalilu.lmusic.viewmodel.NetworkDataViewModel

/**
 * 监听Flow进行更新
 */
//@Composable
//fun rememberCoverWithFlow(
//    mediaItem: MediaItem,
//    networkDataViewModel: NetworkDataViewModel = hiltViewModel(),
//    builder: ImageRequest.Builder.() -> Unit = {}
//): ImagePainter {
//    val data = networkDataViewModel.getNetworkDataFlowByMediaId(mediaItem.mediaId)
//        .collectAsState(initial = null).value?.requireCoverUri()
//        ?: Library.getSongOrNull(mediaItem.mediaId)
//
//    return rememberImagePainter(
//        data = data,
//        builder = builder
//    )
//}

/**
 * 单次查询
 */
//@Composable
//fun rememberCoverForOnce(
//    mediaItem: MediaItem,
//    networkDataViewModel: NetworkDataViewModel = hiltViewModel(),
//    builder: ImageRequest.Builder.() -> Unit = {}
//): ImagePainter {
//    var data by remember { mutableStateOf<Any?>(null) }
//
//    LaunchedEffect(mediaItem) {
//        data = networkDataViewModel.getNetworkDataByMediaId(mediaItem.mediaId)
//            ?.requireCoverUri() ?: Library.getSongOrNull(mediaItem.mediaId)
//    }
//
//    return rememberImagePainter(
//        data = data,
//        builder = builder
//    )
//}
