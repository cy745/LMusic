package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.lmusic.adapter.LMusicPlaylistAdapter
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.PlaylistFragmentViewModel
import com.lalilu.media.entity.Music
import com.lalilu.media.entity.Playlist

class PlaylistFragment : BaseFragment() {
    private lateinit var mState: PlaylistFragmentViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var mAdapter: LMusicPlaylistAdapter
    private lateinit var playerModule: LMusicPlayerModule
    override var delayLoadDuration: Long = 100

    override fun initViewModel() {
        mState = getFragmentViewModel(PlaylistFragmentViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
        playerModule = LMusicPlayerModule.getInstance(mActivity!!.application)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter = LMusicPlaylistAdapter()
        mAdapter.setOnItemLongClickListener { adapter, _, position ->
            try {
                val node = adapter.data[position] as FirstNode<*>
                val playlist = node.data as Playlist

                mEvent.nowPlaylistId.value = playlist.playlistId
            } catch (e: Exception) {
                println(e.message)
            }
            true
        }
        mAdapter.setOnItemChildClickListener { adapter, _, position ->
            val node = adapter.data[position] as SecondNode<*>
            val music = node.data as Music

            playerModule.mediaController.value?.transportControls
                ?.playFromMediaId(music.musicId.toString(), null)
        }

        return DataBindingConfig(R.layout.fragment_play_list, BR.vm, mState)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun loadInitData() {
        mAdapter.setEmptyView(R.layout.item_empty_view)

        mEvent.allPlaylistRequest.getData().observe(viewLifecycleOwner) {
            mState.playlist.postValue(it.map { playlist ->
                FirstNode(playlist.musics?.map { music ->
                    SecondNode(null, music)
                }, playlist.playlist)
            }.toMutableList())
        }
        mEvent.allPlaylistRequest.requestData()
    }
}