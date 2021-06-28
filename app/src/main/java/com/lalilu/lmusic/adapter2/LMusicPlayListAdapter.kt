package com.lalilu.lmusic.adapter2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.ItemPlayListItemBinding
import com.lalilu.databinding.ItemPlayListItemExpandBinding
import com.lalilu.lmusic.fragment.LMusicViewModel
import com.lalilu.media.entity.LMusicPlayList
import java.util.*

class LMusicPlayListAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PLAYLIST_COLLAPSED = 0
        const val PLAYLIST_EXPANDED = 1
    }

    var playLists = mutableListOf<LMusicPlayList>()
    var expandItem = -1

    fun onItemClick(position: Int) {

        val oldPosition = if (position != expandItem) expandItem else -1
        expandItem = if (position != expandItem) position else -1

        if (expandItem != -1) {
            val mViewModel = LMusicViewModel.getInstance(null)
            val recyclerView = mViewModel.mPlayListRecyclerView.value ?: return
            val appBar = mViewModel.mAppBar.value ?: return

//            val smoothScroller = SmoothScrollerToTop(context)
//            smoothScroller.targetPosition = position
//            recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            appBar.setExpanded(false, true)
        }
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            PLAYLIST_EXPANDED -> {
                val binding = (holder as PlayListItemExpandHolder).binding
                binding.playList = playLists[position]
//                binding.childList.adapter =
//                    ArrayAdapter(
//                        context,
//                        R.layout.item_play_list_item_expand_list_item,
//                        playLists[position].mediaIdList.values.toList()
//                    )
            }
            else -> {
                val binding = (holder as PlayListItemHolder).binding
                binding.playList = playLists[position]

//                val map = playLists[position].mediaIdList
//                binding.playListFirst.text = getItemInLinkedHashMapByIndex(map, 0)
//                binding.playListSecond.text = getItemInLinkedHashMapByIndex(map, 1)
//                binding.playListThird.text = getItemInLinkedHashMapByIndex(map, 2)
            }
        }
    }

    private fun <T> getItemInLinkedHashMapByIndex(map: LinkedHashMap<T, T>, index: Int): T? {
        val iterator = map.entries.iterator()
        var result: T? = null
        for (i: Int in 0..index) {
            if (iterator.hasNext()) {
                result = iterator.next().value
            }
        }
        return result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PLAYLIST_EXPANDED -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_play_list_item_expand, parent, false)
                PlayListItemExpandHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_play_list_item, parent, false)
                PlayListItemHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == expandItem) PLAYLIST_EXPANDED else PLAYLIST_COLLAPSED
    }

    override fun getItemCount(): Int = playLists.size

    inner class PlayListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemPlayListItemBinding = ItemPlayListItemBinding.bind(itemView)

        init {
            binding.root.setOnClickListener { onItemClick(this.bindingAdapterPosition) }
        }
    }

    inner class PlayListItemExpandHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemPlayListItemExpandBinding = ItemPlayListItemExpandBinding.bind(itemView)

        init {
            binding.root.setOnClickListener { onItemClick(this.bindingAdapterPosition) }
        }
    }

    inner class SmoothScrollerToTop(context: Context) : LinearSmoothScroller(context) {

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
}