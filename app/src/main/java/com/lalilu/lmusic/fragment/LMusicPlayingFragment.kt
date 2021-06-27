package com.lalilu.lmusic.fragment

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.lalilu.R
import com.lalilu.common.LMusicList
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.player.LMusicPlayerModule


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