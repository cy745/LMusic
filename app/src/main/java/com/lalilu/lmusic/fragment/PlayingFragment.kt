package com.lalilu.lmusic.fragment

import android.support.v4.media.session.PlaybackStateCompat
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.adapter.OnItemDragAdapter
import com.lalilu.lmusic.adapter.OnItemSwipedAdapter
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.PlayingFragmentViewModel
import com.lalilu.lmusic.utils.Mathf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class PlayingFragment : BaseFragment() {
    private lateinit var mState: PlayingFragmentViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var mAdapter: MSongPlayingAdapter
    private lateinit var playerModule: LMusicPlayerModule
    private lateinit var database: LMusicDataBase
    private var positionTimer: Timer? = null

    override fun initViewModel() {
        mState = getFragmentViewModel(PlayingFragmentViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
        playerModule = LMusicPlayerModule.getInstance(mActivity!!.application)
        database = LMusicDataBase.getInstance(requireActivity().applicationContext)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = MSongPlayingAdapter()
        mAdapter.draggableModule.isDragEnabled = true
        mAdapter.draggableModule.isSwipeEnabled = true
        mAdapter.draggableModule.setOnItemDragListener(object : OnItemDragAdapter() {
            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mEvent.nowPlaylistWithSongsRequest.postData(mEvent.nowPlaylistWithSongsRequest.getData().value.also {
                    it?.songs = ArrayList(mAdapter.data)
                })
            }
        })
        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipedAdapter() {
            var mediaId: Long = 0
            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mediaId = mAdapter.getItem(pos).songId
            }

            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mEvent.nowPlaylistWithSongsRequest.postData(mEvent.nowPlaylistWithSongsRequest.getData().value.also {
                    it?.songs = ArrayList(mAdapter.data)
                })
            }
        })

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val song = adapter.data[position] as MSong

            playerModule.mediaController.value?.transportControls
                ?.playFromMediaId(song.songId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_now_playing, BR.vm, mState)
            .addParam(BR.playingAdapter, mAdapter)
    }

    override fun loadInitData() {
        mEvent.nowPlaylistWithSongsRequest.getData().observe(viewLifecycleOwner) {
            mState.playlistWithSongs.value = it
        }

        mState.nowBgPalette.observe(viewLifecycleOwner) {
            mEvent.nowBgPalette.postValue(it)
        }
        mEvent.nowMSongRequest.getData().observe(viewLifecycleOwner) {
            it?.let { mState.playingMSong.postValue(it) }
        }
        mState.playingMSong.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.IO) {
                val detail = database.songDetailDao().getById(it.songId) ?: return@launch
                val lyric = detail.songLyric ?: return@launch

                launch(Dispatchers.Main) {
                    val binding = (mBinding as FragmentNowPlayingBinding)
                    binding.fmLyricViewX.loadLyric(lyric)
                }
            }
        }
        playerModule.playBackState.observe(viewLifecycleOwner) {
            it ?: return@observe
            var currentDuration = Mathf.getPositionFromPlaybackStateCompat(it)
            val binding = (mBinding as FragmentNowPlayingBinding)

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

    override fun loadInitView() {
        val binding = (mBinding as FragmentNowPlayingBinding)
        mActivity!!.setSupportActionBar(binding.fmToolbar)
        mAdapter.draggableModule.attachToRecyclerView(binding.nowPlayingRecyclerView)
        binding.fmLyricViewX.setLabel("暂无歌词")
    }
}