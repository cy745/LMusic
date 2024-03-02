package com.lalilu.lplaylist.entity

import io.fastkv.interfaces.FastEncoder
import io.packable.PackCreator
import io.packable.PackDecoder
import io.packable.PackEncoder
import io.packable.Packable

data class LPlaylist(
    val id: String,
    val title: String,
    val subTitle: String,
    val coverUri: String,
    val mediaIds: List<String>
) : Packable {
    override fun encode(encoder: PackEncoder) {
        encoder.putString(0, id)
            .putString(1, title)
            .putString(2, subTitle)
            .putString(3, coverUri)
            .putStringList(4, mediaIds)
    }

    companion object CREATOR : PackCreator<LPlaylist> {
        override fun decode(decoder: PackDecoder): LPlaylist? {
            val id = decoder.getString(0) ?: return null
            val title = decoder.getString(1) ?: "Unknown"
            val subTitle = decoder.getString(2) ?: ""
            val coverUri = decoder.getString(3) ?: ""
            val mediaIds = decoder.getStringList(4) ?: emptyList()

            return LPlaylist(
                id = id,
                title = title,
                subTitle = subTitle,
                coverUri = coverUri,
                mediaIds = mediaIds
            )
        }
    }
}

object LPlaylistFastEncoder : FastEncoder<LPlaylist> {
    override fun tag(): String = "LPlaylistFastEncoder"

    override fun decode(bytes: ByteArray, offset: Int, length: Int): LPlaylist {
        return PackDecoder.unmarshal(bytes, offset, length, LPlaylist.CREATOR)
    }

    override fun encode(obj: LPlaylist): ByteArray {
        return PackEncoder.marshal(obj)
    }
}