package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.chad.library.adapter.base.BaseNodeAdapter
import com.lalilu.R
import com.lalilu.lmusic.adapter.LMusicPlaylistAdapter
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.base.BaseFragment
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import com.lalilu.lmusic.state.PlaylistFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : BaseFragment() {
    private lateinit var mState: PlaylistFragmentViewModel
    private lateinit var mEvent: SharedViewModel
    private lateinit var mAdapter: LMusicPlaylistAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    override fun initViewModel() {
        mState = getFragmentViewModel(PlaylistFragmentViewModel::class.java)
        mEvent = getApplicationViewModel(SharedViewModel::class.java)
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

                val playlist = parent.data as MPlaylist
                val song = child.data as MSong

                mEvent.nowPlaylistId.value = playlist.playlistId
                playerModule.mediaController.value?.transportControls
                    ?.playFromMediaId(song.songId.toString(), null)
            }

            override fun onDeleted(playlistId: Long) {
                // TODO: 2021/11/29 替代
//                LMusicPlaylistMMKV.getInstance().deletePlaylistById(playlistId)
                mEvent.allPlaylistRequest.requireData()
            }
        }

        return DataBindingConfig(R.layout.fragment_playlist, BR.vm, mState)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun loadInitData() {
        // 根据获取到的 playlists 构建歌单在 RecyclerView 中的层级结构
        mEvent.allPlaylistRequest.getData().observe(viewLifecycleOwner) {
            mState.playlist.postValue(it.map { playlist ->
                FirstNode(playlist.songs.map { song ->
                    SecondNode(null, song)
                }, playlist.playlist)
            })
        }
        // 请求更新获取 playlists
        mEvent.allPlaylistRequest.requireData()
    }

    override fun loadInitView() {
        GlobalScope.launch(Dispatchers.Main) {
            mAdapter.setEmptyView(R.layout.item_empty_view)
        }
    }
}