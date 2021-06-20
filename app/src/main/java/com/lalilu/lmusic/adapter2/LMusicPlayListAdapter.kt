package com.lalilu.lmusic.adapter2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.ItemPlayListItemBinding
import com.lalilu.media.entity.LMusicPlayList
import java.util.*

class LMusicPlayListAdapter(private val context: Context) :
    RecyclerView.Adapter<LMusicPlayListAdapter.PlayListItemHolder>() {
    var playLists = mutableListOf<LMusicPlayList>()

    override fun onBindViewHolder(holder: PlayListItemHolder, position: Int) {
        holder.binding.playListTitle.text = playLists[position].playListTitle

        val treeSet = playLists[position].mediaIdList
        holder.binding.playListFirst.text = getTreeSetItemByIndex(treeSet, 0)
        holder.binding.playListSecond.text = getTreeSetItemByIndex(treeSet, 1)
        holder.binding.playListThird.text = getTreeSetItemByIndex(treeSet, 2)
    }

    private fun <T> getTreeSetItemByIndex(treeSet: TreeSet<T>, index: Int): T? {
        val size = treeSet.size
        return if (index <= size - 1) treeSet.elementAt(index) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListItemHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_play_list_item, parent, false)
        return PlayListItemHolder(view)
    }

    override fun getItemCount(): Int = playLists.size

    inner class PlayListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemPlayListItemBinding = ItemPlayListItemBinding.bind(itemView)
    }
}