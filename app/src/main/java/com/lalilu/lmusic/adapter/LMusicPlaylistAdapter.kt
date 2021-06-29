package com.lalilu.lmusic.adapter

import android.view.View
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lalilu.R
import com.lalilu.lmusic.ui.SamplingDraweeView
import com.lalilu.media.entity.Music
import com.lalilu.media.entity.PlaylistWithMusics

class LMusicPlaylistAdapter : BaseNodeAdapter() {
    companion object {
        const val NODE_PLAYLIST = 0
        const val NODE_MUSIC = 1
        const val PAY_LOAD = 2
    }

    init {
        addFullSpanNodeProvider(RootNodeProvider())
        addNodeProvider(SecondNodeProvider())
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is PlaylistWithMusics -> NODE_PLAYLIST
            is Music -> NODE_MUSIC
            else -> -1
        }
    }

    class RootNodeProvider : BaseNodeProvider() {
        override val itemViewType: Int = NODE_PLAYLIST
        override val layoutId: Int = R.layout.item_playlist

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val playlist = (item as PlaylistWithMusics).playlist ?: return
            val lastTime = System.currentTimeMillis() - playlist.playlistCreateTime
            val min = lastTime / 1000 / 60
            val result = when {
                min < 5 -> "刚刚"
                min in 5..59 -> "$min 分前"
                min > 60 -> "${min / 60} 小时前"
                else -> "···"
            }
            helper.setText(R.id.playlist_title, playlist.playlistTitle)
            helper.setText(R.id.playlist_last_time, result)
            helper.getView<SamplingDraweeView>(R.id.playlist_pic)
                .setImageURI(playlist.playlistArt, context)
        }

        override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
            if (!payloads.contains(PAY_LOAD)) {
                convert(helper, item)
            }
        }

        override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
            getAdapter()?.expandOrCollapse(
                position,
                animate = true,
                notify = true,
                parentPayload = listOf(PAY_LOAD)
            )
        }
    }

    class SecondNodeProvider : BaseNodeProvider() {
        override val itemViewType: Int = NODE_MUSIC
        override val layoutId: Int = R.layout.item_playlist_expand

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val music = item as Music
            helper.setText(R.id.music_order, helper.layoutPosition.toString())
            helper.setText(R.id.music_title, music.musicTitle)
        }
    }
}

