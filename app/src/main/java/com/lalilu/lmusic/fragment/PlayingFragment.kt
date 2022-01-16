package com.lalilu.lmusic.fragment

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.appbar.AppBarLayout
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setItems
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.LMusicPlayerModule
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
            val info = adapter.data[position] as FullSongInfo

            playerModule.mediaController?.transportControls
                ?.playFromMediaId(info.song.songId.toString(), null)
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
        val fmTips = binding.fmTips

        dataModule.songDetail.observe(viewLifecycleOwner) {
            fmLyricViewX.setLabel("暂无歌词")
            fmLyricViewX.setCurrentTextSize(dp2px(requireContext(), 18))
            fmLyricViewX.loadLyric(it?.songLyric)
        }
        dataModule.songPosition.observe(viewLifecycleOwner) { position ->
            fmLyricViewX.updateTime(position)
        }
        playerModule.mSongsLiveData.observe(viewLifecycleOwner) {
            mAdapter.setItems(it)
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
                val size = list.filter { it.state == WorkInfo.State.SUCCEEDED }.size
                val max = list.size
                fmTips.text = "$size / $max"
            }
    }

    private fun dp2px(context: Context, value: Int): Float {
        val v = context.resources.displayMetrics.density
        return v * value + 0.5f
    }
}