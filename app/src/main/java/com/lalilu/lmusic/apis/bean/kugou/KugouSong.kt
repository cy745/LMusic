package com.lalilu.lmusic.apis.bean.kugou

import com.lalilu.lmusic.apis.NetworkSearchResponse
import com.lalilu.lmusic.apis.NetworkSong
import com.lalilu.lmusic.apis.PLATFORM_KUGOU

data class KugouSearchSongResponse(
    val status: Int,
    val errcode: Int,
    val error: String,
    val data: KugouSearchSongData
) : NetworkSearchResponse {
    override val songs: List<NetworkSong>
        get() = data.info
}

data class KugouSearchSongData(
    val total: Int,
    val istag: Int,
    val tab: String,
    val allowerr: Int,
    val timestamp: Long,
    val istagresult: Int,
    val correctiontype: Int,
    val forcecorrection: Int,
    val correctiontip: String,
//     val aggregation: [],
    val info: List<KugouSong>
)

data class KugouSong(
    val hash: String,
    val sqfilesize: Long,
    val sourceid: Int,
    val pay_type_sq: Int,
    val bitrate: Int,
    val ownercount: Int,
    val pkg_price_sq: Int,
    val songname: String,
    val album_name: String,
    val songname_original: String,
    val Accompany: Int,
    val sqhash: String,
    val fail_process: Int,
    val pay_type: Int,
    val rp_type: String,
    val album_id: String,
    val othername_original: String,
    val mvhash: String,
    val extname: String,
    val `320hash`: String,
    val price_320: Int,
    val topic: String?,
    val singername: String,
    val duration: Int
) : NetworkSong {
    override val songId: String
        get() = hash
    override val songAlias: String?
        get() = topic?.takeIf { it.isNotEmpty() } ?: songname_original
    override val songTitle: String
        get() = songname
    override val songArtist: String
        get() = singername
    override val songAlbum: String
        get() = album_name
    override val songDuration: Long
        get() = duration * 1000L
    override val fromPlatform: Int
        get() = PLATFORM_KUGOU
}