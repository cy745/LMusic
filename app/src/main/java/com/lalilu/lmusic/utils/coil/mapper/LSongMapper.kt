package com.lalilu.lmusic.utils.coil.mapper

import coil.map.Mapper
import coil.request.Options
import com.lalilu.lmedia.entity.LSong

class LSongMapper : Mapper<LSong, Any> {
    override fun map(data: LSong, options: Options): Any? {
        // 若artworkUri是http或https的链接，则返回该uri，否则返回LSong
        return data.artworkUri?.takeIf { it.scheme == "http" || it.scheme == "https" }
            ?: data
    }
}