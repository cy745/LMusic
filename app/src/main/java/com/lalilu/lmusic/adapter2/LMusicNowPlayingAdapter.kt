package com.lalilu.lmusic.adapter2

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.common.LMusicList
import com.lalilu.common.Mathf
import com.lalilu.databinding.ItemSongMediaItemBinding
import com.lalilu.lmusic.fragment.LMusicViewModel
import java.util.*

class LMusicNowPlayingAdapter(private val context: Context) :
    RecyclerView.Adapter<LMusicNowPlayingAdapter.SongHolder>() {

    private val mList: LMusicListInAdapter<String, MediaBrowserCompat.MediaItem> =
        LMusicListInAdapter()

    var itemOnClick: ((mediaItem: MediaBrowserCompat.MediaItem) -> Unit)? = null
    var itemOnLongClick: ((mediaItem: MediaBrowserCompat.MediaItem) -> Unit)? = null
    var itemOnMove: ((mediaId: String) -> Unit)? = null
    var itemOnSwiped: ((mediaId: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_song_media_item, parent, false)
        return SongHolder(view, itemOnClick, itemOnLongClick)
    }

    fun onItemMove(viewHolder: RecyclerView.ViewHolder): Boolean {
        val mediaItem = (viewHolder as LMusicNowPlayingAdapter.SongHolder).binding.mediaItem
            ?: return true
        val mediaId = mediaItem.mediaId ?: return true
        itemOnMove?.let { it(mediaId) }
        return true
    }

    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder) {
        val mediaItem = (viewHolder as LMusicNowPlayingAdapter.SongHolder).binding.mediaItem
            ?: return
        val mediaId = mediaItem.mediaId ?: return
        mList.mOrderList.remove(mediaId)
        mList.updateShowList()
        itemOnSwiped?.let { it(mediaId) }
    }

    override fun getItemCount(): Int = mList.size()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.mediaItem = mList.getShowItemByPosition(position)
    }

    inner class SongHolder(
        itemView: View,
        itemOnClick: ((mediaItem: MediaBrowserCompat.MediaItem) -> Unit)?,
        itemOnLongClick: ((mediaItem: MediaBrowserCompat.MediaItem) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongMediaItemBinding.bind(itemView)

        init {
            binding.root.setOnClickListener {
                binding.mediaItem?.let {
                    if (itemOnClick != null) itemOnClick(it)
                }
            }
            binding.root.setOnLongClickListener {
                binding.mediaItem?.let {
                    if (itemOnLongClick != null) itemOnLongClick(it)
                }
                true
            }
        }
    }

    fun setDataIn(list: List<MediaBrowserCompat.MediaItem>) {
        list.forEach { mList.setValueIn(it.mediaId ?: return, it) }
        mList.updateShowList()
    }

    fun updateByMetadata(metadata: MediaMetadataCompat?) {
        metadata ?: return
        when (metadata.description?.extras?.get(LMusicList.LIST_TRANSFORM_ACTION)) {
            LMusicList.ACTION_JUMP_TO -> mList.jumpTo(metadata.description.mediaId)
            LMusicList.ACTION_MOVE_TO -> mList.moveTo(metadata.description.mediaId)
            else -> mList.moveTo(metadata.description.mediaId ?: return)
        }
    }

    inner class LMusicListInAdapter<K, V> : LMusicList<K, V>() {
        private var mShowList = LinkedList<K>()
        fun getShowItemByPosition(position: Int): V? {
            return mDataList[mShowList[position]]
        }

        override fun playByKey(key: K?): V? {
            return super.playByKey(key).also { updateShowList() }
        }

        override fun jumpTo(key: K?): V? {
            return super.jumpTo(key).also { updateShowList() }
        }

        override fun moveTo(key: K?): V? {
            return super.moveTo(key).also { updateShowList() }
        }

        override fun setValueIn(key: K?, value: V?) {
            super.setValueIn(key, value).also { updateShowList() }
        }

        fun updateShowList() {
            val temp = LinkedList<K>()
            for (i in 0 until mOrderList.size) {
                temp.add(i, mOrderList[Mathf.clampInLoop(0, mOrderList.size - 1, mNowPosition, i)])
            }
            val result = DiffUtil.calculateDiff(getDiffCallBack(temp))
            result.dispatchUpdatesTo(this@LMusicNowPlayingAdapter)
            mShowList = temp
            LMusicViewModel.getInstance(null)
                .mNowPlayingRecyclerView.value?.scrollToPosition(0)
        }

        private fun getDiffCallBack(newList: List<K>): Companion.LMusicListDiffCallback<K> {
            return Companion.LMusicListDiffCallback(mShowList, newList)
        }
    }
}
