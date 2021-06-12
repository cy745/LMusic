package com.lalilu.lmusic.adapter2

import android.app.Activity
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.LMusicList
import com.lalilu.lmusic.R
import com.lalilu.lmusic.databinding.ItemSongMediaItemBinding
import com.lalilu.lmusic.service2.MusicBrowser
import java.util.*

class MusicListAdapter(
    private val context: Activity,
    private val musicBrowser: MusicBrowser
) : RecyclerView.Adapter<MusicListAdapter.SongHolder>() {

    private val mediaItemList: LMusicList<String, MediaBrowserCompat.MediaItem> = LMusicList()
    private lateinit var itemClickListener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_song_media_item, parent, false)
        return SongHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int = mediaItemList.size()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.binding.mediaItem = mediaItemList.getSelectedByPosition(position)
    }

    private lateinit var recyclerView: RecyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    private fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }

    fun setOnItemClickListener(listener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit) {
        this.itemClickListener = listener
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
    }

    private fun swapMediaItem(mediaId: String?) {
        val temp = LinkedList(mediaItemList.getOrderList())
        LMusicList.swapSelectedToBottom(temp, oldMetaData?.description?.mediaId ?: return)
        LMusicList.swapSelectedToTop(temp, mediaId ?: return)
        updateListOrder(temp)
    }

    private var oldMetaData: MediaMetadataCompat? = null
    fun setDataIn(list: List<MediaBrowserCompat.MediaItem>) {
        list.forEach {
            mediaItemList.setValueIn(it.mediaId ?: return, it)
        }
        musicBrowser.mediaMetadataCompat.observeForever {
            if (it == null) return@observeForever
            swapMediaItem(it.description.mediaId)
            oldMetaData = it
        }
        notifyDataSetChanged()
        val temp = LinkedList(list.map { it.mediaId ?: return })
        updateListOrder(temp)
    }

    private fun updateListOrder(list: LinkedList<String>) {
        val result = DiffUtil.calculateDiff(mediaItemList.getDiffCallBack(list), true)
        result.dispatchUpdatesTo(this@MusicListAdapter)
        scrollToTop()
        mediaItemList.setNewOrderList(list)
    }
}
