package com.lalilu.lmusic.adapter

import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistBinding
import com.lalilu.lmusic.datasource.entity.MPlaylist
import javax.inject.Inject

class AddSongToPlaylistsAdapter @Inject constructor() :
    BaseAdapter<MPlaylist, ItemPlaylistBinding>(R.layout.item_playlist) {
    val selectedSet: LinkedHashSet<MPlaylist> = LinkedHashSet()

    override fun onBind(binding: ItemPlaylistBinding, item: MPlaylist) {
        binding.title = item.playlistTitle
        binding.root.isSelected = getIsSelected(item)
    }

    private fun getIsSelected(item: MPlaylist): Boolean {
        return selectedSet.contains(item)
    }
}