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
import com.lalilu.lmusic.datasource.extensions.getSongData
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import com.lalilu.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
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

    private lateinit var dialog: NavigatorFragment

    private val defaultSlogan: String by lazy {
        requireActivity().resources.getString(R.string.default_slogan)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = {
            println("play: onItemClick: ${System.currentTimeMillis()}")
            val index = mAdapter.tempList.indexOfFirst { item -> item == it.mediaId }

            mSongBrowser.browser?.apply {
                println("play: seekToPosition: ${System.currentTimeMillis()}")
                seekToDefaultPosition(index)
                prepare()
                play()
            }
        }
        dialog = NavigatorFragment()
        mAdapter.onItemLongClick = { song ->
            showDialog(dialog) {
                (this as NavigatorFragment)
                    .getNavController(singleUse = true)
                    .navigate(
                        LibraryFragmentDirections
                            .libraryToSongDetail(song.mediaId.toLong())
                    )
            }
        }
        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.ev, mEvent)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentPlayingBinding
        val fmAppbarLayout = binding.fmAppbarLayout
        mActivity?.setSupportActionBar(binding.fmToolbar)

        mSongBrowser.mediaMetadataLiveData.observe(viewLifecycleOwner) {
            val text = if (TextUtils.isEmpty(it.title)) defaultSlogan else it.title
            if (binding.fmCollapseLayout.title != text) {
                binding.fmCollapseLayout.title = text
            }
            binding.fmTopPic.setCoverSourceUri(it.mediaUri)
            launch(Dispatchers.IO) {
                val lyric = EmbeddedDataUtils.loadLyric(it.getSongData())
                withContext(Dispatchers.Main) {
                    binding.fmLyricViewX.loadLyric(lyric, null)
                }
            }
        }
        mSongBrowser.currentPositionLiveData.observe(viewLifecycleOwner) {
            binding.fmLyricViewX.updateTime(it)
        }
        mSongBrowser.playlistLiveData.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it.toMutableList())
        }
        mSongBrowser.originPlaylistIdLiveData.observe(viewLifecycleOwner) {
            mAdapter.tempList = it
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