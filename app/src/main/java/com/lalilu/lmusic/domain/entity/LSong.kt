package com.lalilu.lmusic.domain.entity

import android.net.Uri
import android.os.Parcelable
import android.text.TextUtils
import kotlinx.parcelize.Parcelize

@Parcelize
@Deprecated("后期删除")
data class LSong(
    var mId: Long,
    var mTitle: String,
    var mType: Int = SONG_TYPE_LOCAL,
    var mArtUri: Uri? = null,
    var mArtist: List<Artist>? = null,
    var mAlbum: AlbumInfo? = null,
    var mLocalInfo: LocalInfo? = null
) : Parcelable {

    fun getArtistText(): String {
        if (this.mArtist == null) return "null"
        return TextUtils.join(" / ", this.mArtist!!.map { it.name })
    }

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
        var mUri: Uri,
        var mSize: Long = 0,
        var mDuration: Long = 0,
        var mMimeType: String? = null,
        var mLyric: String? = null
    ) : Parcelable

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

        if (mId != other.mId) return false

        return true
    }

    override fun hashCode(): Int {
        return mId.hashCode()
    }
}