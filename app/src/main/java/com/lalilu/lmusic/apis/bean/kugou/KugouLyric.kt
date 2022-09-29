package com.lalilu.lmusic.apis.bean.kugou

import com.lalilu.lmusic.apis.NetworkLyric
import com.lalilu.lmusic.apis.PLATFORM_KUGOU
import okio.ByteString.Companion.decodeBase64


data class KugouLyricResponse(
    val candidates: List<KugouLyricItem>
) {
    fun getKugouLyricItem(): KugouLyricItem? = candidates.getOrNull(0)
}

data class KugouLyricItem(
    val id: String,
    val accesskey: String
)


data class KugouLyric(
    val content: String,
    val info: String
) : NetworkLyric {
    override val mainLyric: String?
        get() = kotlin.runCatching { content.decodeBase64()?.utf8() }.getOrNull()
    override val translateLyric: String?
        get() = null
    override val fromPlatform: Int
        get() = PLATFORM_KUGOU
}