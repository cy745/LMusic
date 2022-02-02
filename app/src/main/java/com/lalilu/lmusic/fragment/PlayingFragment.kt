package com.lalilu.lmusic.fragment

import com.google.android.material.appbar.AppBarLayout
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setItems
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.event.SharedViewModel
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
            .addParam(BR.playingAdapter, mAdapter)
            .addParam(BR.playerModule, playerModule)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmToolbar = binding.fmToolbar
        val fmLyricViewX = binding.fmLyricViewX
        val fmAppbarLayout = binding.fmAppbarLayout
        val recyclerView = binding.nowPlayingRecyclerView

        mActivity?.setSupportActionBar(fmToolbar)
        dataModule.songLyric.observe(viewLifecycleOwner) {
            fmLyricViewX.loadLyric(it)
        }
        dataModule.songPosition.observe(viewLifecycleOwner) { position ->
            fmLyricViewX.updateTime(position)
        }
        playerModule.mSongsLiveData.observe(viewLifecycleOwner) {
            mAdapter.setItems(it, recyclerView)
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