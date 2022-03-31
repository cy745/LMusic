package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import coil.loadAny
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.GsonUtils
import com.lalilu.R
import com.lalilu.databinding.ItemShareItemBinding
import com.lalilu.lmusic.apis.bean.ShareDto
import com.lalilu.lmusic.service.STATE_LISTENING
import com.lalilu.lmusic.utils.fetcher.getBase64Cover
import io.ably.lib.types.Message
import javax.inject.Inject

class ShareAdapter @Inject constructor() :
    BaseAdapter<Message, ItemShareItemBinding>(R.layout.item_share_item) {
    override val itemCallback: DiffUtil.ItemCallback<Message>
        get() = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.connectionId == newItem.connectionId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.connectionId == newItem.connectionId &&
                        oldItem.id == newItem.id &&
                        oldItem.data.equals(newItem.data)
            }
        }

    override fun onBind(binding: ItemShareItemBinding, item: Message, position: Int) {
        binding.name = item.connectionId
        binding.state = item.name

        when (item.name) {
            STATE_LISTENING -> try {
                binding.songCardBg.visibility = View.VISIBLE
                val shareDto = GsonUtils.fromJson(item.data.toString(), ShareDto::class.java)
                binding.musicTitle = shareDto.title
                binding.musicArtist = shareDto.artist

                shareDto.coverBaseColor?.let(binding.songCardBg::setCardBackgroundColor)
                shareDto.coverBase64?.getBase64Cover()?.let(binding.songCardCover::loadAny)
                val isLightBg =
                    ColorUtils.isLightColor(binding.songCardBg.cardBackgroundColor.defaultColor)
                binding.songCardTitle.setTextColor(if (isLightBg) Color.BLACK else Color.WHITE)
            } catch (e: Exception) {
                binding.musicTitle = "加载失败"
                binding.musicArtist = "${e.message}"
                binding.songCardCover.setImageResource(R.drawable.ic_error_warning_line)
                e.printStackTrace()
            }
            else -> {
                binding.songCardBg.visibility = View.GONE
            }
        }
    }
}