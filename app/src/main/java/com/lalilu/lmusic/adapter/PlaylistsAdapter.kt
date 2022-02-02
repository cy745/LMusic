package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistBinding
import com.lalilu.lmusic.domain.entity.MPlaylist
import javax.inject.Inject

class PlaylistsAdapter @Inject constructor() :
    BaseAdapter<MPlaylist, ItemPlaylistBinding>(R.layout.item_playing) {

    override val itemCallback: DiffUtil.ItemCallback<MPlaylist>
        get() = object : DiffUtil.ItemCallback<MPlaylist>() {
            override fun areItemsTheSame(oldItem: MPlaylist, newItem: MPlaylist): Boolean {
                return oldItem.playlistId == newItem.playlistId
            }

            override fun areContentsTheSame(oldItem: MPlaylist, newItem: MPlaylist): Boolean {
                return oldItem.playlistId == newItem.playlistId &&
                        oldItem.playlistTitle == newItem.playlistTitle &&
                        oldItem.playlistInfo == newItem.playlistInfo &&
                        oldItem.playlistCoverUri == newItem.playlistCoverUri
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]

        binding.title = item.playlistTitle
        binding.coverUri = item.playlistCoverUri
    }
}
