package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.domain.entity.LSong

@Deprecated("MSongPlayingAdapter 替代，后期删除")
class LMusicPlayingAdapter :
    BaseQuickAdapter<LSong, BaseDataBindingHolder<ItemPlayingBinding>>(
        R.layout.item_playing
    ), DraggableModule {

    override fun convert(holder: BaseDataBindingHolder<ItemPlayingBinding>, item: LSong) {
        val binding = holder.dataBinding ?: return
        binding.title = item.mTitle
        binding.duration = item.mLocalInfo?.mDuration ?: 0L
        binding.artist = item.getArtistText()
        binding.mimeType = item.mType.toString()
        binding.coverUri = item.mArtUri
    }


    init {
        setDiffCallback(DiffSong())
        setEmptyView(R.layout.item_empty_view)
    }

    class DiffSong : DiffUtil.ItemCallback<LSong>() {
        override fun areItemsTheSame(oldItem: LSong, newItem: LSong): Boolean {
            return oldItem.mId == newItem.mId
        }

        override fun areContentsTheSame(oldItem: LSong, newItem: LSong): Boolean {
            return oldItem.mId == newItem.mId &&
                    oldItem.mTitle == newItem.mTitle
        }
    }
}