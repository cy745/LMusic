package com.lalilu.lmusic.utils.coil.keyer

import androidx.media3.common.MediaItem
import coil3.key.Keyer
import coil3.request.Options
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong


class LSongCoverKeyer : Keyer<LSong> {
    override fun key(data: LSong, options: Options): String {
        return "LSONG_${data.id}_${options.size.width}_${options.size.height}"
    }
}

class LAlbumCoverKeyer : Keyer<LAlbum> {
    override fun key(data: LAlbum, options: Options): String {
        return "LALBUM_${data.id}_${options.size.width}_${options.size.height}"
    }
}

class MediaItemKeyer : Keyer<MediaItem> {
    override fun key(data: MediaItem, options: Options): String {
        return data.mediaId
    }
}