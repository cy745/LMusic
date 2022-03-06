package com.lalilu.lmusic.viewmodel

import androidx.media3.common.MediaItem
import javax.inject.Inject

/**
 * 为AlbumsFragment保存数据的ViewModel
 *
 */
class AlbumsViewModel @Inject constructor() : BaseViewModel<List<MediaItem>>() {
}