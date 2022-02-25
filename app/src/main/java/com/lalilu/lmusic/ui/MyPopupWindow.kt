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
import com.lalilu.lmusic.adapter.AddSongToPlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingPopupWindow
import com.lalilu.lmusic.domain.entity.MPlaylist

class MyPopupWindow constructor(
    context: Context,
    private val callback: (List<MPlaylist>) -> Unit
) : DataBindingPopupWindow(context) {
    lateinit var mAdapter: AddSongToPlaylistsAdapter

    fun setData(data: MutableList<MPlaylist>) {
        mAdapter.data = data
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = AddSongToPlaylistsAdapter()
        return DataBindingConfig(R.layout.popup_add_to_playlist)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onDismiss(cancel: Boolean) {
        if (!cancel) callback(mAdapter.selectedSet.toMutableList())
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
        binding.popupAddToPlaylistCancel.setOnClickListener {
            dismiss(true)
        }
        binding.popupAddToPlaylistConfirm.setOnClickListener {
            dismiss(false)
        }
        mAdapter.onItemClick = {
            val index = mAdapter.data.indexOf(it)
            val checkedSet = mAdapter.selectedSet
            if (checkedSet.contains(it)) checkedSet.remove(it)
            else checkedSet.add(it)
            mAdapter.notifyItemChanged(index)

            binding.popupAddToPlaylistTips.text = if (checkedSet.size == 0) "添加至歌单"
            else "已选中: ${checkedSet.size}"
        }
    }
}