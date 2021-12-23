package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.chad.library.adapter.base.BaseNodeAdapter
import com.lalilu.R
import com.lalilu.lmusic.adapter.LMusicPlaylistAdapter
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.adapter.node.SecondNode
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.service.LMusicPlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlaylistFragment : DataBindingFragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mAdapter: LMusicPlaylistAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
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

                launch(Dispatchers.IO) {
                    dataModule._nowPlaylistId.emit(playlist.playlistId)
                }
                playerModule.mediaController?.transportControls
                    ?.playFromMediaId(song.songId.toString(), null)
            }

            override fun onDeleted(playlistId: Long) {
                // TODO: 2021/11/29 替代
            }
        }

        return DataBindingConfig(R.layout.fragment_playlist)
            .addParam(BR.dataModule, dataModule)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun onViewCreated() {
        mAdapter.setEmptyView(R.layout.item_empty_view)
    }
}