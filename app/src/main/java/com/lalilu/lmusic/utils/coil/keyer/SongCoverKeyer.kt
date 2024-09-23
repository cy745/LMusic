package com.lalilu.lmusic.utils.coil.keyer

import androidx.media3.common.MediaItem
import coil3.key.Keyer
import coil3.request.Options
import com.lalilu.common.base.Playable
import com.lalilu.lmedia.entity.Item

class SongCoverKeyer : Keyer<Item> {
    override fun key(data: Item, options: Options): String {
        return "${data::class.simpleName}_${data.id}"
    }
}

class PlayableKeyer : Keyer<Playable> {
    override fun key(data: Playable, options: Options): String {
        return "${data::class.simpleName}_${data.mediaId}"
    }
}

class MediaItemKeyer : Keyer<MediaItem> {
    override fun key(data: MediaItem, options: Options): String {
        return data.mediaId
    }
}