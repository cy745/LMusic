package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.PlayingFragmentViewModel
import com.lalilu.lmusic.utils.Mathf
import com.lalilu.lmusic.utils.OnItemDragAdapter
import com.lalilu.lmusic.utils.OnItemSwipedAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class PlayingFragment : BaseFragment() {
    private lateinit var mState: PlayingFragmentViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var mAdapter: LMusicPlayingAdapter
    private lateinit var playerModule: LMusicPlayerModule
    private var positionTimer: Timer? = null

    override var delayLoadDuration: Long = 100

    override fun initViewModel() {
        mState = getFragmentViewModel(PlayingFragmentViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
        playerModule = LMusicPlayerModule.getInstance(mActivity!!.application)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = LMusicPlayingAdapter()
        mAdapter.draggableModule.isDragEnabled = true
        mAdapter.draggableModule.isSwipeEnabled = true
        mAdapter.draggableModule.setOnItemDragListener(object : OnItemDragAdapter() {
            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mEvent.nowPlaylistRequest.postData(mEvent.nowPlaylistRequest.getData().value.also {
                    it?.songs = ArrayList(mAdapter.data)
                })
            }
        })
        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipedAdapter() {
            var mediaId: Long = 0
            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mediaId = mAdapter.getItem(pos).mId
            }

            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mEvent.nowPlaylistRequest.postData(mEvent.nowPlaylistRequest.getData().value.also {
                    it?.songs = ArrayList(mAdapter.data)
                })
            }
        })

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val song = adapter.data[position] as LSong

            playerModule.mediaController.value?.transportControls
                ?.playFromMediaId(song.mId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_now_playing, BR.vm, mState)
            .addParam(BR.playingAdapter, mAdapter)
    }

    override fun loadInitData() {
        mAdapter.draggableModule.attachToRecyclerView((mBinding as FragmentNowPlayingBinding).nowPlayingRecyclerView)

        mEvent.nowPlaylistRequest.getData().observe(viewLifecycleOwner) {
            mState.musicList.value = it
        }

        val binding = (mBinding as FragmentNowPlayingBinding)
//        binding.fmTabLayout.bindToViewPager(binding.fmViewpager)
        binding.fmLyricViewX.setLabel("暂无歌词")

        mState.nowBgPalette.observe(viewLifecycleOwner) {
            mEvent.nowBgPalette.postValue(it)
        }
        mEvent.nowPlayingRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.nowPlayingMusic.postValue(it) }
        }
        mState.nowPlayingMusic.observe(viewLifecycleOwner) {
            val lyric = it.mLocalInfo?.mLyric
            binding.fmLyricViewX.loadLyric(lyric)
        }
        playerModule.playBackState.observe(viewLifecycleOwner) {
            it ?: return@observe
            var currentDuration = Mathf.getPositionFromPlaybackStateCompat(it)

            positionTimer?.cancel()
            if (it.state == PlaybackStateCompat.STATE_PLAYING)
                positionTimer = Timer().apply {
                    this.schedule(0, 1000) {
                        binding.fmLyricViewX.updateTime(currentDuration)
                        currentDuration += 1000
                    }
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = (mBinding as FragmentNowPlayingBinding)
        mActivity!!.setSupportActionBar(binding.fmToolbar)
    }
}