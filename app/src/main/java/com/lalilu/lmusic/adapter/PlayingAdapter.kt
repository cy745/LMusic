package com.lalilu.lmusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class PlayingAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var songs: MutableList<MSong> = ArrayList()
    var onItemClickListener: ((item: MSong) -> Unit)? = null

    class PlayingViewHolder(val binding: ItemPlayingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemPlayingBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_playing,
            parent, false
        )
        return PlayingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as PlayingViewHolder).binding
        val item = songs[position]

        binding.song = item
        binding.hasLyric = true
        binding.root.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = songs.size
}