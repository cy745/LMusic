package com.lalilu.media.entity

import android.net.Uri
import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class LSong(
    var mId: Long,
    var mTitle: String,
    var mType: Int = SONG_TYPE_LOCAL,
    var mArtUri: String? = null,
    var mArtist: List<Artist>? = null,

    @Embedded
    var mAlbum: AlbumInfo? = null,

    @Embedded
    var mLocalInfo: LocalInfo? = null

) : Parcelable {

    companion object {
        const val SONG_TYPE_LOCAL = 0
        const val SONG_TYPE_NETWORK = 1

    }

    @Parcelize
    data class AlbumInfo(
        var albumId: Long = 0,
        var albumTitle: String? = null
    ) : Parcelable

    /**
     * 本地信息
     */
    @Parcelize
    data class LocalInfo(
        var mData: String,
        var mSize: Long = 0,
        var mDuration: Long = 0,
        var mMimeType: String? = null
    ) : Parcelable {
        fun getUri(): Uri = Uri.parse(mData)
    }

    /**
     * 本地信息
     */
    @Parcelize
    data class Artist(
        var name: String
    ) : Parcelable


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LSong

        if (mTitle != other.mTitle) return false
        if (mType != other.mType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mTitle.hashCode()
        result = 31 * result + mType
        return result
    }
}