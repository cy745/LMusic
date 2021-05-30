package com.lalilu.lmusic.adapter2

import android.app.Activity
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.R
import com.lalilu.lmusic.databinding.ItemSongMediaItemBinding
import com.lalilu.lmusic.utils.MediaItemDiffCallback
import java.util.*

interface UpdatableAdapter<T> {
    fun updateList(list: List<T>)
    fun setOnItemClickListener(listener: (mediaItem: T) -> Unit)
}

class MusicListAdapter(
    private val context: Activity,
) : RecyclerView.Adapter<MusicListAdapter.SongHolder>(),
    UpdatableAdapter<MediaBrowserCompat.MediaItem> {

    private var mediaItemList: List<MediaBrowserCompat.MediaItem> = ArrayList()
    private lateinit var itemClickListener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_song_media_item, parent, false)
        return SongHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int = mediaItemList.size

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.mediaItem = mediaItemList[position]
    }

    private lateinit var recyclerView: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    inner class SongHolder(
        itemView: View,
        itemClickListener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongMediaItemBinding.bind(itemView)
        var removeBtn: ImageButton = binding.songRemove

        init {
            binding.root.setOnClickListener {
                binding.mediaItem?.let { itemClickListener(it) }
                swapMediaItem()
            }
            binding.root.setOnLongClickListener {
                Toast.makeText(
                    context,
                    (binding.mediaItem as MediaBrowserCompat.MediaItem).toString(),
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        }

        private fun swapMediaItem() {
            val temp = LinkedList(mediaItemList)
            val mediaItem = binding.mediaItem as MediaBrowserCompat.MediaItem
            temp.remove(mediaItem)
            temp.addFirst(mediaItem)
            setList(temp)
            scrollToTop()
        }
    }

    fun setList(list: List<MediaBrowserCompat.MediaItem>) {
        val result = DiffUtil.calculateDiff(MediaItemDiffCallback(mediaItemList, list), true)
        result.dispatchUpdatesTo(this)
        mediaItemList = list
    }

    override fun updateList(list: List<MediaBrowserCompat.MediaItem>) {
        val result = DiffUtil.calculateDiff(MediaItemDiffCallback(mediaItemList, list), true)
        result.dispatchUpdatesTo(this)
        mediaItemList = list
    }

    override fun setOnItemClickListener(listener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit) {
        this.itemClickListener = listener
    }
}
