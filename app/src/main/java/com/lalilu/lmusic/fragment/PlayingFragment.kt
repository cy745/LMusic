package com.lalilu.lmusic.fragment

import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.base.showDialog
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.manager.LyricManager
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import com.lalilu.material.appbar.ExpendHeaderBehavior
import com.lalilu.material.appbar.MyAppbarBehavior
import com.lalilu.material.appbar.STATE_COLLAPSED
import com.lalilu.material.appbar.STATE_NORMAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlayingFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var lyricManager: LyricManager

    @Inject
    lateinit var mGlobal: GlobalViewModel

    @Inject
    lateinit var mState: PlayingViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: PlayingAdapter

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    private val dialog: NavigatorFragment by lazy {
        NavigatorFragment()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemClick = {
            if (mSongBrowser.playById(it.mediaId)) {
                mSongBrowser.browser?.apply {
                    prepare()
                    play()
                }
            }
        }
        mAdapter.onItemLongClick = {
            showDialog(dialog) {
                (this as NavigatorFragment)
                    .navigateFrom(R.id.songDetailFragment)
                    .navigate(
                        LibraryFragmentDirections.libraryToSongDetail(it.mediaId)
                    )
            }
        }
        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.vm, mState)
            .addParam(BR.ev, mEvent)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmAppbarLayout = binding.fmAppbarLayout
        val fmLyricViewX = binding.fmLyricViewX
        val fmToolbar = binding.fmToolbar
        val behavior = fmAppbarLayout.behavior as MyAppbarBehavior

        mActivity?.setSupportActionBar(fmToolbar)
        behavior.addOnStateChangeListener(object :
            ExpendHeaderBehavior.OnScrollToStateListener(STATE_COLLAPSED, STATE_NORMAL) {
            override fun onScrollToStateListener() {
                if (fmToolbar.hasExpandedActionView())
                    fmToolbar.collapseActionView()
            }
        })
        mGlobal.currentPlaylistLiveData.observe(viewLifecycleOwner) {
            mState.postData(it)
        }
        mGlobal.currentMediaItemLiveData.observe(viewLifecycleOwner) {
            mState.song.postValue(it)
        }
        mGlobal.currentPositionLiveData.observe(viewLifecycleOwner) {
            fmLyricViewX.updateTime(it)
        }
        lyricManager.songLyric.observe(viewLifecycleOwner) {
            fmLyricViewX.setLyricEntryList(emptyList())
            fmLyricViewX.loadLyric(it?.first, it?.second)
        }
        mEvent.isAppbarLayoutExpand.observe(viewLifecycleOwner) {
            it?.get { fmAppbarLayout.setExpanded(false, true) }
        }
    }
}