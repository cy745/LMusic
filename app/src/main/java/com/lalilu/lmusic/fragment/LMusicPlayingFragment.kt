package com.lalilu.lmusic.fragment

import android.graphics.Canvas
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.lalilu.R
import com.lalilu.common.LMusicList.Companion.ACTION_MOVE_TO
import com.lalilu.common.LMusicList.Companion.LIST_TRANSFORM_ACTION
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music
import com.lalilu.player.LMusicPlayerModule
import com.lalilu.player.service.LMusicService.Companion.ACTION_MOVED_SONG
import com.lalilu.player.service.LMusicService.Companion.ACTION_SWIPED_SONG
import com.lalilu.player.service.LMusicService.Companion.NEW_MEDIA_ORDER_LIST
import java.util.*


class LMusicPlayingFragment : Fragment(R.layout.fragment_now_playing) {
    private lateinit var mRecyclerView: AntiMisOperationRecyclerView
    private lateinit var mAdapter: LMusicPlayingAdapter
    private lateinit var mediaModule: LMusicMediaModule
    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule
    private var mediaControllerCompat: MediaControllerCompat? = null
    private var listAdjustment = false

    private fun bindToMediaController() {
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as Music
            mediaControllerCompat?.transportControls?.playFromMediaId(
                item.musicId.toString(), Bundle().also {
                    it.putInt(LIST_TRANSFORM_ACTION, ACTION_MOVE_TO)
                })
        }
        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipeListener {
            var mediaId: String? = null
            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mediaId = mAdapter.getItem(pos).musicId.toString()
            }

            override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}
            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                println(mediaId ?: return)
                listAdjustment = true
                mediaControllerCompat?.transportControls?.sendCustomAction(
                    ACTION_SWIPED_SONG, Bundle().also {
                        it.putString(
                            MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId
                        )
                    })
            }

            override fun onItemSwipeMoving(
                canvas: Canvas?,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                isCurrentlyActive: Boolean
            ) {
            }
        })
        mAdapter.draggableModule.setOnItemDragListener(object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}
            override fun onItemDragMoving(
                source: RecyclerView.ViewHolder?, from: Int,
                target: RecyclerView.ViewHolder?, to: Int
            ) {
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                val map = mAdapter.data.map { it.musicId.toString() }
                listAdjustment = true
                mediaControllerCompat?.transportControls?.sendCustomAction(
                    ACTION_MOVED_SONG, Bundle().also {
                        it.putStringArrayList(NEW_MEDIA_ORDER_LIST, ArrayList(map))
                    })
            }
        })
    }

    private fun initializeObserver() {
        playerModule.mediaList.observeForever { mediaIdList ->
            mediaIdList ?: return@observeForever
            val dao = mediaModule.database.musicDao()
            val media = mediaIdList.map {
                dao.getMusicById(it) ?: Music(0)
            }.toMutableList()
            if (listAdjustment) {
                mAdapter.setDiffNewData(media)
                listAdjustment = false
            } else mAdapter.setDiffNewData(media) {
                mRecyclerView.scrollToPosition(0)
            }
        }
        playerModule.mediaController.observeForever { it?.let { mediaControllerCompat = it } }
        mViewModel.mPlayingRecyclerView.postValue(mRecyclerView)
        mViewModel.mPlayingAdapter.postValue(mAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRecyclerView = FragmentNowPlayingBinding.bind(view).nowPlayingRecyclerView
        mRecyclerView.adapter = LMusicPlayingAdapter()
        mAdapter = mRecyclerView.adapter as LMusicPlayingAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.setEmptyView(R.layout.item_empty_view)
        mAdapter.setDiffCallback(DiffMusic())
        mAdapter.animationEnable = true
        mAdapter.isAnimationFirstOnly = false
        mAdapter.draggableModule.isDragEnabled = true
        mAdapter.draggableModule.isSwipeEnabled = true
        mAdapter.draggableModule.attachToRecyclerView(mRecyclerView)

        mediaModule = LMusicMediaModule.getInstance(null)
        mViewModel = LMusicViewModel.getInstance(null)
        playerModule = LMusicPlayerModule.getInstance(null)

        bindToMediaController()
        initializeObserver()
    }

    inner class DiffMusic : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.musicId == newItem.musicId
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.musicId == newItem.musicId &&
                    oldItem.musicTitle == newItem.musicTitle
        }
    }
}