package com.lalilu.lmusic.fragment

import com.google.android.material.appbar.AppBarLayout
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.fragment.viewmodel.PlayingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlayingFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mState: PlayingViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: PlayingAdapter

    @Inject
    lateinit var playerModule: PlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClickListener = {
            playerModule.mediaController?.transportControls
                ?.playFromMediaId(it.songId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.ev, mEvent)
            .addParam(BR.vm, mState)
            .addParam(BR.playingAdapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmAppbarLayout = binding.fmAppbarLayout
        mActivity?.setSupportActionBar(binding.fmToolbar)

        playerModule.metadataLiveData.observe(viewLifecycleOwner) {
            mState._title.postValue(it?.description?.title.toString())
            mState._mediaUri.postValue(it?.description?.mediaUri)
        }
        dataModule.songLyric.observe(viewLifecycleOwner) {
            mState._lyric.postValue(it)
        }
        dataModule.songPosition.observe(viewLifecycleOwner) {
            mState._position.postValue(it)
        }
        playerModule.mSongsLiveData.observe(viewLifecycleOwner) {
            mState._songs.postValue(it)
        }
        mEvent.isAppbarLayoutExpand.observe(viewLifecycleOwner) {
            it?.get { fmAppbarLayout.setExpanded(false, true) }
        }

        var lastOffset = 0
        fmAppbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appbarLayout, verticalOffset ->
            if ((lastOffset - verticalOffset < 0) && verticalOffset >= (-appbarLayout.totalScrollRange * 3 / 4))
                mEvent.collapseSearchView()
            lastOffset = verticalOffset
        })
    }
}