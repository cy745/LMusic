package com.lalilu.lmusic.fragment

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setMediaItems
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
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
    lateinit var mAdapter: MSongPlayingAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var dataModule: DataModule

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
        val fmToolbar = (mBinding as FragmentPlayingBinding).fmToolbar
        val fmLyricViewX = (mBinding as FragmentPlayingBinding).fmLyricViewX
        val fmAppbarLayout = (mBinding as FragmentPlayingBinding).fmAppbarLayout
        val fmTips = (mBinding as FragmentPlayingBinding).fmTips

        dataModule.songDetail.observe(viewLifecycleOwner) {
            fmLyricViewX.setLabel("暂无歌词")
            fmLyricViewX.setCurrentTextSize(dp2px(requireContext(), 18))
            fmLyricViewX.loadLyric(it?.songLyric)
        }
        dataModule.songPosition.observe(viewLifecycleOwner) { position ->
            fmLyricViewX.updateTime(position)
        }
        playerModule.mediaItemsLiveData.observe(viewLifecycleOwner) {
            mAdapter.setMediaItems(it)
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


        //        mAdapter.draggableModule.attachToRecyclerView(binding.nowPlayingRecyclerView)

        mActivity?.setSupportActionBar(fmToolbar)

        WorkManager.getInstance(requireContext()).getWorkInfosByTagLiveData("Save_Song")
            .observe(viewLifecycleOwner) { list ->
                fmTips.text = list.filter { it.state == WorkInfo.State.SUCCEEDED }.size.toString()
            }
    }

    private fun dp2px(context: Context, value: Int): Float {
        val v = context.resources.displayMetrics.density
        return v * value + 0.5f
    }
}