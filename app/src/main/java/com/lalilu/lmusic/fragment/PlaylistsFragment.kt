package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.lalilu.R
import com.lalilu.databinding.FragmentListPlaylistsBinding
import com.lalilu.lmusic.adapter.PlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.SongInPlaylist
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.viewmodel.PlaylistsViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
import com.lalilu.lmusic.viewmodel.savePosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PlaylistsFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mAdapter: PlaylistsAdapter

    @Inject
    lateinit var mState: PlaylistsViewModel

    @Inject
    lateinit var dataBase: LMusicDataBase

    @Inject
    lateinit var mediaBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemLongClick = {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().delete(it)
            }
        }
        mAdapter.onItemClick = {
            mAdapter.savePosition(mState)
            findNavController().navigate(
                PlaylistsFragmentDirections.toPlaylistDetail(
                    playlistId = it.playlistId,
                    playlistTitle = it.playlistTitle
                )
            )
        }
        return DataBindingConfig(R.layout.fragment_list_playlists)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentListPlaylistsBinding

        dataBase.playlistDao().getAllLiveDataSortByTime().observe(viewLifecycleOwner) {
            mState.postData(it)
        }

        binding.playlistCopyCurrentButton.setOnClickListener {
            launch(Dispatchers.IO) {
                val playlistTitle = "复制歌单: (${mediaBrowser.originPlaylistIds.size})"
                val playlist = MPlaylist(playlistTitle = playlistTitle)
                dataBase.playlistDao().save(playlist)
                dataBase.songInPlaylistDao().save(
                    mediaBrowser.originPlaylistIds.map {
                        SongInPlaylist(
                            playlistId = playlist.playlistId,
                            mediaId = it
                        )
                    }
                )
            }
        }
        binding.playlistCreateButton.setOnClickListener {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().save(
                    MPlaylist(playlistTitle = "空歌单")
                )
            }
        }
    }
}