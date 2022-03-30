package com.lalilu.lmusic.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.ShareAdapter
import com.lalilu.lmusic.apis.bean.ShareDto
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.service.AblyService
import com.lalilu.lmusic.viewmodel.ShareViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareFragment : DataBindingFragment() {

    @Inject
    lateinit var ablyService: AblyService

    @Inject
    lateinit var mAdapter: ShareAdapter

    @Inject
    lateinit var mState: ShareViewModel

    private val clipboardManager: ClipboardManager by lazy {
        requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemLongClick = { msg, _ ->
            try {
                val shareDto = GsonUtils.fromJson(msg.data.toString(), ShareDto::class.java)
                val clip = ClipData.newPlainText(
                    "LMusic_COPY", "${shareDto.title} ${shareDto.artist}"
                )
                clipboardManager.setPrimaryClip(clip)
                ToastUtils.showLong("复制成功")
            } catch (e: Exception) {
                ToastUtils.showLong("复制失败")
                e.printStackTrace()
            }
        }
        return DataBindingConfig(R.layout.fragment_share)
            .addParam(BR.shareAdapter, mAdapter)
    }

    override fun onViewCreated() {
        ablyService.historyLiveData.observe(viewLifecycleOwner) {
            it ?: return@observe
            mState.postData(it.values.toList())
        }
    }
}