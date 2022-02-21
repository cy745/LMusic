package com.lalilu.lmusic.fragment

import androidx.databinding.ViewDataBinding
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.DialogSongDetailBinding
import com.lalilu.lmusic.base.BaseBottomSheetFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.fragment.viewmodel.SongDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SongDetailFragment : BaseBottomSheetFragment<MSong, DialogSongDetailBinding>() {

    @Inject
    lateinit var mState: SongDetailViewModel

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.dialog_song_detail)
            .addParam(BR.vm, mState)
    }

    override fun onBind(data: MSong?, binding: ViewDataBinding) {
        if (data != null) mState._song.postValue(data)
    }
}