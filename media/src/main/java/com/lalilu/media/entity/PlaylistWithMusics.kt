package com.lalilu.media.entity

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

data class PlaylistWithMusics(
    @Embedded var playlist: Playlist? = null,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "musicId",
        associateBy = Junction(PlaylistMusicCrossRef::class)
    )
    var musics: List<Music>? = null,
) : BaseExpandNode() {
    @Ignore
    override val childNode: MutableList<BaseNode>? = this.musics?.toMutableList()

    @Ignore
    override var isExpanded: Boolean = false
}