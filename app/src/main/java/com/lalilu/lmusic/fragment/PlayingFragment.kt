package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import android.text.TextUtils
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.base.showDialog
import com.lalilu.lmusic.binding_adapter.setCoverSourceUri
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
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
@SuppressLint("UnsafeOptInUsageError")
@ExperimentalCoroutinesApi
class PlayingFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: PlayingAdapter

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    private val dialog: NavigatorFragment by lazy {
        NavigatorFragment()
    }

    private val defaultSlogan: String by lazy {
        requireActivity().resources.getString(R.string.default_slogan)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
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
                    .getNavController(singleUse = true)
                    .navigate(
                        LibraryFragmentDirections
                            .libraryToSongDetail(it.mediaId)
                    )
            }
        }
        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.ev, mEvent)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmCollapseLayout = binding.fmCollapseLayout
        val fmAppbarLayout = binding.fmAppbarLayout
        val fmLyricViewX = binding.fmLyricViewX
        val fmToolbar = binding.fmToolbar
        val fmTopPic = binding.fmTopPic
        mActivity?.setSupportActionBar(fmToolbar)
        val behavior = fmAppbarLayout.behavior as MyAppbarBehavior
        behavior.addOnStateChangeListener(object :
            ExpendHeaderBehavior.OnScrollToStateListener(STATE_COLLAPSED, STATE_NORMAL) {
            override fun onScrollToStateListener() {
                if (fmToolbar.hasExpandedActionView())
                    fmToolbar.collapseActionView()
            }
        })
        mSongBrowser.currentMediaItemLiveData.observe(viewLifecycleOwner) {
            val title = it?.mediaMetadata?.title
            val text = if (TextUtils.isEmpty(title)) defaultSlogan else title
            if (fmCollapseLayout.title != text) fmCollapseLayout.title = text
            fmTopPic.setCoverSourceUri(it?.mediaMetadata?.mediaUri)
        }
        mSongBrowser.currentLyricLiveData.observe(viewLifecycleOwner) {
            fmLyricViewX.loadLyric(it?.first, it?.second)
        }
        mSongBrowser.currentPositionLiveData.observe(viewLifecycleOwner) {
            fmLyricViewX.updateTime(it)
        }
        mSongBrowser.playlistLiveData.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it.toMutableList())
        }
        mEvent.isAppbarLayoutExpand.observe(viewLifecycleOwner) {
            it?.get { fmAppbarLayout.setExpanded(false, true) }
        }
    }
}