package com.lalilu.media.entity

import android.net.Uri
import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class LSong(
    var mTitle: String,
    var mType: Int = SONG_TYPE_LOCAL,
    var mArtUri: String? = null,
    var mArtist: ArrayList<Artist>? = null,

    @Embedded
    var mLocalInfo: LocalInfo?

) : Parcelable {

    companion object {
        const val SONG_TYPE_LOCAL = 0
        const val SONG_TYPE_NETWORK = 1
    }

    /**
     * 本地信息
     */
    @Parcelize
    data class LocalInfo(
        var mData: String,
        var mSize: Long = 0,
        var mDuration: Long = 0
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