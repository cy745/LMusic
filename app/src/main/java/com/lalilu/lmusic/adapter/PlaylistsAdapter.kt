package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistBinding
import com.lalilu.lmusic.datasource.entity.MPlaylist
import javax.inject.Inject

class PlaylistsAdapter @Inject constructor() :
    BaseAdapter<MPlaylist, ItemPlaylistBinding>(R.layout.item_playlist) {

    override val itemCallback: DiffUtil.ItemCallback<MPlaylist>
        get() = object : DiffUtil.ItemCallback<MPlaylist>() {
            override fun areItemsTheSame(oldItem: MPlaylist, newItem: MPlaylist): Boolean {
                return oldItem.playlistId == newItem.playlistId
            }

            override fun areContentsTheSame(oldItem: MPlaylist, newItem: MPlaylist): Boolean {
                return oldItem.playlistId == newItem.playlistId &&
                        oldItem.playlistTitle == newItem.playlistTitle &&
                        oldItem.playlistInfo == newItem.playlistInfo
            }
        }

    override fun onBind(binding: ItemPlaylistBinding, item: MPlaylist, position: Int) {
        binding.title = item.playlistTitle
    }
}
