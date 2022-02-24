package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.PopupAddToPlaylistBinding
import com.lalilu.lmusic.adapter.PlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingPopupWindow
import com.lalilu.lmusic.domain.entity.MPlaylist

class MyPopupWindow constructor(
    context: Context,
    private val callback: (List<MPlaylist>) -> Unit
) : DataBindingPopupWindow(context) {
    lateinit var mAdapter: PlaylistsAdapter
    private val checkData: LinkedHashSet<MPlaylist> = LinkedHashSet()

    fun setData(data: MutableList<MPlaylist>) {
        mAdapter.data = data
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = PlaylistsAdapter()
        return DataBindingConfig(R.layout.popup_add_to_playlist)
            .addParam(BR.adapter, mAdapter)
    }

    override fun dismiss() {
        super.dismiss()
        callback(checkData.toList())
    }

    override fun getTargetView(bd: ViewDataBinding): View {
        val binding = bd as PopupAddToPlaylistBinding
        return binding.popupAddToPlaylistDialog
    }

    override fun onViewCreated(bd: ViewDataBinding) {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        animationStyle = R.style.MY_PopupWindowStyle
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val binding = bd as PopupAddToPlaylistBinding
        mAdapter.onItemClick = {
            if (checkData.contains(it)) checkData.remove(it)
            else checkData.add(it)
            binding.popupAddToPlaylistTips.text = if (checkData.size == 0) "添加至歌单"
            else "已选中: ${checkData.size}"
        }
    }
}