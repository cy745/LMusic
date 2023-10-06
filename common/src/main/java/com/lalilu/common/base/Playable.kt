package com.lalilu.common.base

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat

/**
 * 通用可播放元素
 *
 * 为了实现播放功能和各种元素之间解耦合，定义了可播放元素接口，
 * 实现该接口的元素，可以被播放器播放，并且可以被各种元素引用，
 * 通过使用id进行区分，将该元素区分为不同的元素
 */
interface Playable {
    val mediaId: String
    val title: String
    val subTitle: String
    val durationMs: Long

    val targetUri: Uri
    val imageSource: Any?
    val sticker: List<Sticker>

    val metaDataCompat: MediaMetadataCompat
}