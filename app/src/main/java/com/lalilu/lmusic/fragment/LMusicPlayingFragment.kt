package com.lalilu.lmusic.fragment

import android.graphics.Canvas
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.lalilu.R
import com.lalilu.common.LMusicList
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.player.LMusicPlayerModule
import com.lalilu.player.service.LMusicService.Companion.ACTION_MOVED_SONG
import com.lalilu.player.service.LMusicService.Companion.ACTION_SWIPED_SONG
import com.lalilu.player.service.LMusicService.Companion.NEW_MEDIA_ORDER_LIST


class LMusicPlayingFragment : Fragment(R.layout.fragment_now_playing) {
    private lateinit var mRecyclerView: AntiMisOperationRecyclerView
    private lateinit var mAdapter: LMusicPlayingAdapter
    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule
    private var mediaControllerCompat: MediaControllerCompat? = null

    private fun bindToMediaController() {
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as MediaBrowserCompat.MediaItem
            mediaControllerCompat?.transportControls?.playFromMediaId(
                item.mediaId, Bundle().also {
                    it.putInt(LMusicList.LIST_TRANSFORM_ACTION, LMusicList.ACTION_MOVE_TO)
                })
        }
        mAdapter.draggableModule.setOnItemSwipeListener(object : OnItemSwipeListener {
            var mediaId: String? = null
            override fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                mediaId = mAdapter.getItem(pos).mediaId
            }

            override fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int) {}
            override fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
                println(mediaId ?: return)
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
                val map = playerModule.mediaList.value?.map { it.mediaId } ?: return
                mediaControllerCompat?.transportControls?.sendCustomAction(
                    ACTION_MOVED_SONG, Bundle().also {
                        it.putStringArrayList(NEW_MEDIA_ORDER_LIST, ArrayList(map))
                    })
            }
        })
    }

    private fun initializeObserver() {
        playerModule.mediaList.observeForever {
            it?.let {
                mAdapter.setDiffNewData(it) {
                    mRecyclerView.scrollToPosition(0)
                }
            }
        }
        playerModule.mediaController.observeForever { it?.let { mediaControllerCompat = it } }
        mViewModel.mNowPlayingRecyclerView.postValue(mRecyclerView)
        mViewModel.mNowPlayingAdapter.postValue(mAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRecyclerView = FragmentNowPlayingBinding.bind(view).nowPlayingRecyclerView
        mRecyclerView.adapter = LMusicPlayingAdapter()
        mAdapter = mRecyclerView.adapter as LMusicPlayingAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.setEmptyView(R.layout.item_empty_view)
        mAdapter.setDiffCallback(DiffMediaItem())
        mAdapter.animationEnable = true
        mAdapter.isAnimationFirstOnly = false
        mAdapter.draggableModule.isDragEnabled = true
        mAdapter.draggableModule.isSwipeEnabled = true
        mAdapter.draggableModule.attachToRecyclerView(mRecyclerView)

        mViewModel = LMusicViewModel.getInstance(null)
        playerModule = LMusicPlayerModule.getInstance(null)

        bindToMediaController()
        initializeObserver()
    }

    inner class DiffMediaItem : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
        override fun areItemsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean {
            return oldItem.mediaId == newItem.mediaId &&
                    oldItem.description.title == newItem.description.title
        }
    }
}