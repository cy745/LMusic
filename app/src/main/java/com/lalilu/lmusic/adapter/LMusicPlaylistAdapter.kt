package com.lalilu.lmusic.adapter

import android.view.View
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lalilu.R
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.ui.SamplingDraweeView
import com.lalilu.media.entity.Music
import com.lalilu.media.entity.Playlist

class LMusicPlaylistAdapter : BaseNodeAdapter() {
    companion object {
        const val NODE_PLAYLIST = 0
        const val NODE_MUSIC = 1
        const val PAY_LOAD = 2
    }

    interface OnSelectedListener {
        fun onSelected(adapter: BaseNodeAdapter, parentPosition: Int, musicPosition: Int)
    }

    var onPlaylistSelectedListener: OnSelectedListener? = null

    init {
        addNodeProvider(RootNodeProvider())
        addNodeProvider(SecondNodeProvider())
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is FirstNode<*> -> NODE_PLAYLIST
            is SecondNode<*> -> NODE_MUSIC
            else -> -1
        }
    }

    class RootNodeProvider : BaseNodeProvider() {
        override val itemViewType: Int = NODE_PLAYLIST
        override val layoutId: Int = R.layout.item_playlist

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val playlist = (item as FirstNode<*>).data as Playlist

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
        }

        override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
            val node = data as BaseExpandNode
            if (node.isExpanded) {
                getAdapter()?.collapse(
                    position,
                    animate = true,
                    notify = true,
                    parentPayload = PAY_LOAD
                )
            } else {
                getAdapter()?.expandAndCollapseOther(
                    position,
                    animate = true,
                    notify = true,
                    expandPayload = PAY_LOAD
                )
            }
        }
    }

    inner class SecondNodeProvider : BaseNodeProvider() {
        override val itemViewType: Int = NODE_MUSIC
        override val layoutId: Int = R.layout.item_playlist_expand

        override fun convert(helper: BaseViewHolder, item: BaseNode) {
            val music = (item as SecondNode<*>).data as Music
            helper.setText(R.id.music_title, music.musicTitle)
            helper.setText(R.id.music_order, music.musicTitle)
        }

        override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
            val adapter = getAdapter() as BaseNodeAdapter
            val parentPosition = adapter.findParentNode(position)

            onPlaylistSelectedListener?.onSelected(adapter, parentPosition, position)
        }
    }
}

