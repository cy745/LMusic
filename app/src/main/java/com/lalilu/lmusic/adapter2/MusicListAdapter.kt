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

class MusicListAdapter(private val context: Activity) :
    RecyclerView.Adapter<MusicListAdapter.SongHolder>() {

    private val mList: LMusicListInAdapter<String, MediaBrowserCompat.MediaItem> =
        LMusicListInAdapter()

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

    private fun scrollToTop() = recyclerView.scrollToPosition(0)

    inner class SongHolder(
        itemView: View,
        itemClickListener: (mediaItem: MediaBrowserCompat.MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        var binding = ItemSongMediaItemBinding.bind(itemView)
        var removeBtn: ImageButton = binding.songRemove

        init {
            binding.root.setOnClickListener { binding.mediaItem?.let { itemOnClick(it) } }
            binding.root.setOnLongClickListener {
                binding.mediaItem?.let { itemOnLongClick(it) }
                true
            }
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
}
