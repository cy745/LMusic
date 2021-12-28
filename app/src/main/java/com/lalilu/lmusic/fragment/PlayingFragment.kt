package com.lalilu.lmusic.fragment

import android.support.v4.media.MediaBrowserCompat
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setMediaItems
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.service.LMusicPlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlayingFragment : DataBindingFragment() {

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: MSongPlayingAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var notificationManager: LMusicNotificationManager

    override fun getDataBindingConfig(): DataBindingConfig {
//        mAdapter.draggableModule.isDragEnabled = true
//        mAdapter.draggableModule.isSwipeEnabled = true
//        mAdapter.draggableModule.setOnItemDragListener(object : OnItemDragAdapter() {
//            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
//                mEvent.nowPlaylistWithSongsRequest.postData(mEvent.nowPlaylistWithSongsRequest.getData().value.also {
//                    it?.songs = ArrayList(mAdapter.data)
//                })
//            }
//        })
//        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipedAdapter() {
//            var mediaId: Long = 0
//            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
//                mediaId = mAdapter.getItem(pos).songId
//            }
//
//            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
//                mEvent.nowPlaylistWithSongsRequest.postData(mEvent.nowPlaylistWithSongsRequest.getData().value.also {
//                    it?.songs = ArrayList(mAdapter.data)
//                })
//            }
//        })

        mAdapter.setOnItemClickListener { adapter, _, position ->
            val song = adapter.data[position] as MediaBrowserCompat.MediaItem

            playerModule.mediaController?.transportControls
                ?.playFromMediaId(song.mediaId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.ev, mEvent)
            .addParam(BR.playingAdapter, mAdapter)
            .addParam(BR.playerModule, playerModule)
    }

    override fun onViewCreated() {
        val fmLyricViewX = (mBinding as FragmentPlayingBinding).fmLyricViewX
        val fmToolbar = (mBinding as FragmentPlayingBinding).fmToolbar

        playerModule.songDetail.observe(viewLifecycleOwner) {
            fmLyricViewX.setLabel("暂无歌词")
            fmLyricViewX.loadLyric(it?.songLyric)
        }
        var lastLyric: String? = ""
        playerModule.songPosition.observe(viewLifecycleOwner) { position ->
            fmLyricViewX.updateTime(position)

            val nowLyric = fmLyricViewX.getCurrentLineLyricEntry()?.text
            if (nowLyric != lastLyric) {
                println(fmLyricViewX.getCurrentLineLyricEntry()?.text)
                nowLyric?.let {
                    notificationManager.updateLyric(it)
                }
                lastLyric = nowLyric
            }
        }
        playerModule.mediaItems.observe(viewLifecycleOwner) {
            mAdapter.setMediaItems(it)
        }

        //        mAdapter.draggableModule.attachToRecyclerView(binding.nowPlayingRecyclerView)

        mActivity?.setSupportActionBar(fmToolbar)
    }
}