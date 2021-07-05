package com.lalilu.lmusic.fragment

import androidx.recyclerview.widget.RecyclerView
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.PlayingFragmentViewModel
import com.lalilu.lmusic.utils.OnItemDragAdapter
import com.lalilu.lmusic.utils.OnItemSwipedAdapter
import com.lalilu.media.entity.Music

class PlayingFragment : BaseFragment() {
    private lateinit var mState: PlayingFragmentViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var mAdapter: LMusicPlayingAdapter
    private lateinit var playerModule: LMusicPlayerModule
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
                mEvent.nowPlaylistRequest.postData(mAdapter.data)
            }
        })
        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipedAdapter() {
            var mediaId: Long = 0
            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mediaId = mAdapter.getItem(pos).musicId
            }

            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mEvent.nowPlaylistRequest.postData(mAdapter.data)
            }
        })

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val music = adapter.data[position] as Music
            playerModule.mediaController.value?.transportControls
                ?.playFromMediaId(music.musicId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_now_playing, BR.vm, mState)
            .addParam(BR.playingAdapter, mAdapter)
    }

    override fun loadInitData() {
        mAdapter.draggableModule.attachToRecyclerView((mBinding as FragmentNowPlayingBinding).nowPlayingRecyclerView)

        mEvent.nowPlaylistRequest.getData().observe(viewLifecycleOwner) {
            mState.musicList.value = it
        }
    }
}