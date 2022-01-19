package com.lalilu.lmusic.fragment

import androidx.appcompat.widget.Toolbar
import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.lmusic.adapter.MSongListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.database.repository.LIST_TYPE_ALBUM
import com.lalilu.lmusic.database.repository.LIST_TYPE_PLAYLIST
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.event.LMusicPlayerModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ListFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val args: ListFragmentArgs by navArgs()

    @Inject
    lateinit var mAdapter: MSongListAdapter

    @Inject
    lateinit var playerModule: LMusicPlayerModule

    @Inject
    lateinit var dataModule: DataModule

    override fun getDataBindingConfig(): DataBindingConfig {
        // 添加 item 被选中时的处理逻辑
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val data = adapter.data[position] as FullSongInfo? ?: return@setOnItemClickListener

            playerModule.mediaController?.transportControls
                ?.playFromMediaId(data.song.songId.toString(), null)
        }
        return DataBindingConfig(R.layout.fragment_list)
            .addParam(BR.listAdapter, mAdapter)
    }

    override fun onViewCreated() {
        launch {
            withContext(Dispatchers.Main) {
                val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar2)
                toolbar.title = args.title
            }
        }

        if (args.albumId >= 0) {
            dataModule.changeType(LIST_TYPE_ALBUM)
            dataModule.changeId(args.albumId)
        } else if (args.playlistId >= 0) {
            dataModule.changeType(LIST_TYPE_PLAYLIST)
            dataModule.changeId(args.playlistId)
        }

        dataModule.library.observe(viewLifecycleOwner) {
            mAdapter.setNewInstance(it?.toMutableList())
        }
    }
}