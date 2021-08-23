package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.chad.library.adapter.base.BaseNodeAdapter
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
        // 添加 item 被选中时的处理逻辑
        // 将选中的 playlist 的 id 传递到 mEvent 中，切换当前正在播放的歌单
        // 将选中的 music 的 musicId 传递给 mediaController，让其根据 musicId 播放指定歌曲
        mAdapter.onPlaylistSelectedListener = object : LMusicPlaylistAdapter.OnSelectedListener {
            override fun onSelected(
                adapter: BaseNodeAdapter,
                parentPosition: Int,
                musicPosition: Int
            ) {
                val parent = adapter.data[parentPosition] as FirstNode<*>
                val child = adapter.data[musicPosition] as SecondNode<*>

                val playlist = parent.data as Playlist
                val music = child.data as Music

                mEvent.nowPlaylistId.value = playlist.playlistId
                playerModule.mediaController.value?.transportControls
                    ?.playFromMediaId(music.musicId.toString(), null)
            }
        }

        return DataBindingConfig(R.layout.fragment_play_list, BR.vm, mState)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun loadInitData() {
        mAdapter.setEmptyView(R.layout.item_empty_view)

        // 根据获取到的 playlists 构建歌单在 RecyclerView 中的层级结构
        mEvent.allPlaylistRequest.getData().observe(viewLifecycleOwner) {
            mState.playlist.postValue(it.map { playlist ->
                FirstNode(playlist.musics?.map { music ->
                    SecondNode(null, music)
                }, playlist.playlist)
            }.toMutableList())
        }
        // 请求更新获取 playlists
        mEvent.allPlaylistRequest.requestData()
    }
}