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
import com.lalilu.R
import com.lalilu.common.LMusicList
import com.lalilu.databinding.FragmentNowPlayingBinding
import com.lalilu.lmusic.adapter2.LMusicNowPlayingAdapter
import com.lalilu.lmusic.ui.AntiMisOperationRecyclerView
import com.lalilu.lmusic.utils.ItemTouchCallback
import com.lalilu.player.LMusicPlayerModule
import com.lalilu.player.service.LMusicService
import jp.wasabeef.recyclerview.animators.FadeInAnimator

@Deprecated("重写简化为LMusicPlayingFragment")
class LMusicNowPlayingFragment : Fragment() {
    private lateinit var mRecyclerView: AntiMisOperationRecyclerView
    private lateinit var mAdapter: LMusicNowPlayingAdapter
    private lateinit var mViewModel: LMusicViewModel
    private lateinit var playerModule: LMusicPlayerModule
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
                LMusicService.ACTION_MOVE_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
        mAdapter.itemOnSwiped = { mediaId ->
            mediaControllerCompat?.transportControls?.sendCustomAction(
                LMusicService.ACTION_SWIPED_SONG,
                Bundle().also { it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId) }
            )
        }
    }

    private fun initializeObserver() {
        playerModule.mediaList.observeForever { it?.let { mAdapter.setDataIn(it) } }
        playerModule.metadata.observeForever { it?.let { mAdapter.updateByMetadata(it) } }
        playerModule.mediaController.observeForever { it?.let { mediaControllerCompat = it } }
        mViewModel.mNowPlayingRecyclerView.postValue(mRecyclerView)
//        mViewModel.mNowPlayingAdapter.postValue(mAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRecyclerView = FragmentNowPlayingBinding.bind(view).nowPlayingRecyclerView
        mRecyclerView.adapter = LMusicNowPlayingAdapter(requireContext())
        mAdapter = mRecyclerView.adapter as LMusicNowPlayingAdapter
        mViewModel = LMusicViewModel.getInstance(null)
        playerModule = LMusicPlayerModule.getInstance(null)

        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.itemAnimator = FadeInAnimator(OvershootInterpolator()).apply {
            this.addDuration = 300
            this.moveDuration = 200
            this.removeDuration = 50
        }
        ItemTouchHelper(ItemTouchCallback(mAdapter))
            .attachToRecyclerView(mRecyclerView)

        bindToMediaController()
        initializeObserver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_now_playing, container, false)
    }
}