package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentListPlaylistsBinding
import com.lalilu.lmusic.adapter.PlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.fragment.viewmodel.PlaylistsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlaylistsFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mAdapter: PlaylistsAdapter

    @Inject
    lateinit var mState: PlaylistsViewModel

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemLongClick = {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().delete(it)
            }
        }
        mAdapter.onItemClick = {
            mState._position.postValue(requireScrollOffset())
            findNavController().navigate(
                PlaylistsFragmentDirections.toPlaylistDetail(
                    playlistId = it.playlistId ?: 0,
                    playlistTitle = it.playlistTitle
                )
            )
        }
        mAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
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

        mState.position.observe(viewLifecycleOwner) {
            it ?: return@observe
            launch {
                delay(50)
                recyclerView.scrollToPosition(it)
            }
        }

        addButton.setOnClickListener {
            launch(Dispatchers.IO) {
                dataBase.playlistDao().save(
                    MPlaylist(playlistTitle = "空歌单")
                )
            }
        }
    }

    private fun requireScrollOffset(): Int {
        if (mBinding == null || mBinding !is FragmentListPlaylistsBinding) {
            return 0
        }
        return (mBinding as FragmentListPlaylistsBinding)
            .playlistsRecyclerView
            .computeVerticalScrollOffset()
    }
}