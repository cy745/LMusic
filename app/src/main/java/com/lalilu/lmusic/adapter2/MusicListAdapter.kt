package com.lalilu.lmusic.adapter2

import android.app.Activity
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.LMusicList
import com.lalilu.lmusic.R
import com.lalilu.lmusic.databinding.ItemSongMediaItemBinding
import com.lalilu.common.Mathf
import java.util.*

interface ItemTouch {
    fun onItemMove(viewHolder: RecyclerView.ViewHolder): Boolean
    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder)
}

open class MusicListAdapter(private val context: Activity) :
    RecyclerView.Adapter<MusicListAdapter.SongHolder>(), ItemTouch {

    private val mList: LMusicListInAdapter<String, MediaBrowserCompat.MediaItem> =
        LMusicListInAdapter()

    lateinit var itemOnClick: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit
    lateinit var itemOnLongClick: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit
    lateinit var itemOnMove: (mediaId: String) -> Unit
    lateinit var itemOnSwiped: (mediaId: String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_song_media_item, parent, false)
        return SongHolder(view, itemOnClick, itemOnLongClick)
    }

    override fun onItemMove(viewHolder: RecyclerView.ViewHolder): Boolean {
        val mediaItem = (viewHolder as MusicListAdapter.SongHolder).binding.mediaItem
            ?: return true
        val mediaId = mediaItem.mediaId ?: return true
        itemOnMove(mediaId)
        return true
    }

    override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder) {
        val mediaItem = (viewHolder as MusicListAdapter.SongHolder).binding.mediaItem
            ?: return
        val mediaId = mediaItem.mediaId ?: return
        mList.mOrderList.remove(mediaId)
        mList.updateShowList()
        itemOnSwiped(mediaId)
    }

    override fun getItemCount(): Int = mList.size()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.mediaItem = mList.getShowItemByPosition(position)
    }

    private lateinit var recyclerView: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    private fun scrollToTop() = recyclerView.scrollToPosition(0)

    inner class SongHolder(
        itemView: View,
        itemOnClick: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit,
        itemOnLongClick: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongMediaItemBinding.bind(itemView)

        init {
            binding.root.setOnClickListener { binding.mediaItem?.let { itemOnClick(it) } }
//            binding.root.setOnLongClickListener {
//                binding.mediaItem?.let { itemOnLongClick(it) }
//                true
//            }
        }
    }

    fun setDataIn(list: List<MediaBrowserCompat.MediaItem>) {
        list.forEach { mList.setValueIn(it.mediaId ?: return, it) }
        mList.updateShowList()
    }

    fun setMetaDataLiveData(mediaMetadataCompat: MutableLiveData<MediaMetadataCompat>) {
        mediaMetadataCompat.observeForever {
            it ?: return@observeForever
            when (it.description?.extras?.get(LMusicList.LIST_TRANSFORM_ACTION)) {
                LMusicList.ACTION_JUMP_TO -> mList.jumpTo(it.description.mediaId)
                LMusicList.ACTION_MOVE_TO -> mList.moveTo(it.description.mediaId)
                else -> mList.moveTo(it.description.mediaId ?: return@observeForever)
            }
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
            val result = DiffUtil.calculateDiff(getDiffCallBack(temp), true)
            result.dispatchUpdatesTo(this@MusicListAdapter)
            mShowList = temp
            scrollToTop()
        }

        private fun getDiffCallBack(newList: List<K>): Companion.LMusicListDiffCallback<K> {
            return Companion.LMusicListDiffCallback(mShowList, newList)
        }
    }
}
