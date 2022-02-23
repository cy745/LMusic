package com.lalilu.lmusic.fragment

import androidx.navigation.fragment.navArgs
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.fragment.viewmodel.SongDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SongDetailFragment : DataBindingFragment() {
    private val args: SongDetailFragmentArgs by navArgs()

    @Inject
    lateinit var mState: SongDetailViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_song_detail)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        mState._song.postValue(
            mediaSource.getSongById(args.songId)
        )
    }
}