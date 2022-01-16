package com.lalilu.lmusic.adapter

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.domain.entity.FullSongInfo
import javax.inject.Inject

class MSongPlayingAdapter @Inject constructor() :
    BaseQuickAdapter<FullSongInfo, BaseDataBindingHolder<ItemPlayingBinding>>(
        R.layout.item_playing
    ), DraggableModule {

    init {
        setDiffCallback(DiffSong())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffSong : DiffUtil.ItemCallback<FullSongInfo>() {
        override fun areItemsTheSame(
            oldItem: FullSongInfo,
            newItem: FullSongInfo
        ): Boolean = oldItem.song.songId == newItem.song.songId

        override fun areContentsTheSame(
            oldItem: FullSongInfo,
            newItem: FullSongInfo
        ): Boolean = oldItem.song.songId == newItem.song.songId &&
                oldItem.song.songTitle == newItem.song.songTitle &&
                oldItem.detail?.songCoverUri == newItem.detail?.songCoverUri &&
                oldItem.detail?.songLyric == newItem.detail?.songLyric
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemPlayingBinding>,
        item: FullSongInfo
    ) {
        val binding = holder.dataBinding ?: return

        binding.title = item.song.songTitle
        binding.artist = item.song.showingArtist
        binding.coverUri = item.detail?.songCoverUri
        binding.duration = item.song.songDuration
        binding.mimeType = item.song.songMimeType
        binding.hasLyric = !TextUtils.isEmpty(item.detail?.songLyric)
    }
}