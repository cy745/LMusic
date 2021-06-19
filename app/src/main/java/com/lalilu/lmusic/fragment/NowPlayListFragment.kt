package com.lalilu.lmusic.fragment

import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentPlayListBinding
import com.lalilu.lmusic.LMusicList
import com.lalilu.lmusic.adapter2.MusicListAdapter
import com.lalilu.lmusic.service2.MusicService
import com.lalilu.lmusic.utils.ItemTouchCallback
import jp.wasabeef.recyclerview.animators.FadeInAnimator

class NowPlayListFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MusicListAdapter
    private lateinit var mViewModel: LMusicViewModel
    private var mediaControllerCompat: MediaControllerCompat? = null

    private fun bindToMediaController() {
        mAdapter.itemOnClick = { mediaItem ->
            mediaControllerCompat?.transportControls?.playFromMediaId(
                mediaItem.mediaId,
                Bundle().also {
                    it.putInt(LMusicList.LIST_TRANSFORM_ACTION, LMusicList.ACTION_MOVE_TO)
                })
        }
        mAdapter.itemOnLongClick = { mediaItem ->
            mediaControllerCompat?.transportControls?.playFromMediaId(
                mediaItem.mediaId,
                Bundle().also {
                    it.putInt(LMusicList.LIST_TRANSFORM_ACTION, LMusicList.ACTION_JUMP_TO)
                })
        }
        mAdapter.itemOnMove = { mediaId ->
            mediaControllerCompat?.transportControls?.sendCustomAction(
                MusicService.ACTION_MOVE_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
        mAdapter.itemOnSwiped = { mediaId ->
            mediaControllerCompat?.transportControls?.sendCustomAction(
                MusicService.ACTION_SWIPED_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
    }

    private fun initializeObserver() {
        mViewModel.mediaList.observeForever { it?.let { mAdapter.setDataIn(it) } }
        mViewModel.mediaController.observeForever { it?.let { mediaControllerCompat = it } }
        mViewModel.metadata.observeForever { it?.let { mAdapter.updateByMetadata(it) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRecyclerView = FragmentPlayListBinding.bind(view).musicRecyclerView
        mRecyclerView.adapter = MusicListAdapter(requireContext())
        mAdapter = mRecyclerView.adapter as MusicListAdapter
        mViewModel = LMusicViewModel.getInstance(null)
        bindToMediaController()

        mRecyclerView.isNestedScrollingEnabled = false
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = FadeInAnimator(OvershootInterpolator()).apply {
            this.addDuration = 300
            this.moveDuration = 200
            this.removeDuration = 50
        }
        ItemTouchHelper(ItemTouchCallback(mAdapter))
            .attachToRecyclerView(mRecyclerView)

        initializeObserver()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }
}