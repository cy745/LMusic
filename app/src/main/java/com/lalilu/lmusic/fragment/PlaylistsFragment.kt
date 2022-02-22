package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentListPlaylistsBinding
import com.lalilu.lmusic.adapter.PlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MPlaylist
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlaylistsFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mAdapter: PlaylistsAdapter

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemLongClick = {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().delete(it)
            }
        }
        return DataBindingConfig(R.layout.fragment_list_playlists)
            .addParam(BR.playlistAdapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentListPlaylistsBinding
        val recyclerView = binding.playlistsRecyclerView
        val addButton = binding.playlistAddBotton

        dataBase.playlistDao().getAllLiveDataSortByTime().observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it.toMutableList())
            recyclerView.scrollToPosition(0)
        }

        addButton.setOnClickListener {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().save(
                    MPlaylist(
                        playlistTitle = System.currentTimeMillis().toString()
                    )
                )
            }
        }
    }
}