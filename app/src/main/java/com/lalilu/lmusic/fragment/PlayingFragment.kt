package com.lalilu.lmusic.fragment

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayingBinding
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setMediaItems
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.utils.Mathf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
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
    lateinit var database: LMusicDataBase

    private var positionTimer: Timer? = null

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

            playerModule.mediaController.transportControls
                ?.playFromMediaId(song.mediaId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_playing)
            .addParam(BR.ev, mEvent)
            .addParam(BR.playingAdapter, mAdapter)
            .addParam(BR.playerModule, playerModule)
    }

    override fun onViewCreated() {
        playerModule.metadata.observe(viewLifecycleOwner) {
            launch(Dispatchers.IO) {
                val mediaId = it?.description?.mediaId ?: return@launch

                val lyric = database.songDetailDao().getByIdStr(mediaId)?.songLyric ?: return@launch

                launch(Dispatchers.Main) {
                    val binding = (mBinding as FragmentPlayingBinding)
                    binding.fmLyricViewX.loadLyric(lyric)
                }
            }
        }
        playerModule.mediaItem.observe(viewLifecycleOwner) {
            mAdapter.setMediaItems(it)
        }

        playerModule.playBackState.observe(viewLifecycleOwner) {
            it ?: return@observe
            var currentDuration = Mathf.getPositionFromPlaybackStateCompat(it)
            val binding = (mBinding as FragmentPlayingBinding)

            positionTimer?.cancel()
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                positionTimer = Timer().apply {
                    this.schedule(0, 1000) {
                        binding.fmLyricViewX.updateTime(currentDuration)
                        currentDuration += 1000
                    }
                }
            } else {
                binding.fmLyricViewX.updateTime(currentDuration)
            }
        }
        //        mAdapter.draggableModule.attachToRecyclerView(binding.nowPlayingRecyclerView)


        val binding = (mBinding as FragmentPlayingBinding)
        mActivity!!.setSupportActionBar(binding.fmToolbar)
        binding.fmLyricViewX.setLabel("暂无歌词")
    }
}