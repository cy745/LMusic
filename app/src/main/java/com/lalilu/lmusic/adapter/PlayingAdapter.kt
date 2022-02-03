package com.lalilu.lmusic.adapter

import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class PlayingAdapter @Inject constructor() :
    BaseAdapter<MSong, ItemPlayingBinding>(R.layout.item_playing) {
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]

        binding.song = item
        binding.root.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }
}