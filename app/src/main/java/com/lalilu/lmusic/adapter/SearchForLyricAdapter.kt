package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemSongWithLyricBinding
import com.lalilu.lmusic.apis.bean.SongSearchSong
import javax.inject.Inject

class SearchForLyricAdapter @Inject constructor() :
    BaseAdapter<SongSearchSong, ItemSongWithLyricBinding>(R.layout.item_song_with_lyric) {
    var singleSelected: SongSearchSong? = null

    override val itemCallback: DiffUtil.ItemCallback<SongSearchSong>
        get() = object : DiffUtil.ItemCallback<SongSearchSong>() {
            override fun areItemsTheSame(
                oldItem: SongSearchSong,
                newItem: SongSearchSong
            ): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: SongSearchSong,
                newItem: SongSearchSong
            ): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.name == newItem.name &&
                        oldItem.album == newItem.album &&
                        oldItem.artists == newItem.artists
            }
        }

    override fun onBind(binding: ItemSongWithLyricBinding, item: SongSearchSong) {
        binding.title = item.name
        binding.album = item.album?.name
        binding.artist = item.artists.joinToString("/") { it.name }
        binding.duration = item.duration
        binding.root.isSelected = getIsSelected(item)
    }

    private fun getIsSelected(item: SongSearchSong): Boolean {
        return singleSelected == item
    }
}