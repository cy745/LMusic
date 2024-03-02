package com.lalilu.lmusic.utils.coil.keyer

import coil.key.Keyer
import coil.request.Options
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