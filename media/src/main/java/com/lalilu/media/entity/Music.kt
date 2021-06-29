package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.chad.library.adapter.base.entity.node.BaseNode

@Entity
data class Music(
    @PrimaryKey(autoGenerate = true)
    var musicId: Long = 0,
    var musicTitle: String = "",
    var musicSize: Long = 0,
    var musicDuration: Long = 0,
    var musicArtist: String = "",
    var musicMimeType: String = "",
    var musicUri: Uri = Uri.EMPTY,
    var albumTitle: String = "",
    var musicArtUri: Uri = Uri.EMPTY,
    @Ignore
    override val childNode: MutableList<BaseNode>? = null,
) : BaseNode()