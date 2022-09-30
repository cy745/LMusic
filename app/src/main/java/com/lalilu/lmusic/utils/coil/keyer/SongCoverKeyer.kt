package com.lalilu.lmusic.utils.coil.keyer

import coil.key.Keyer
import coil.request.Options
import com.lalilu.lmedia.entity.Item

class SongCoverKeyer : Keyer<Item> {
    override fun key(data: Item, options: Options): String {
        return "${data::class.simpleName}: ${data.id}"
    }
}