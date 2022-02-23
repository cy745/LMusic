package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.database.LMusicDataBase
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.fragment.viewmodel.PlaylistDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PlaylistDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: PlaylistDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: PlaylistDetailViewModel

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var dataBase: LMusicDataBase

    @Inject
    lateinit var playerModule: PlayerModule

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = {
            playerModule.mediaController?.transportControls
                ?.playFromMediaId("${it.songId}", null)
        }
        mAdapter.onItemLongClick = {
            findNavController().navigate(
                PlaylistDetailFragmentDirections.playlistToSongDetail(it.songId)
            )
        }
        return DataBindingConfig(R.layout.fragment_detail_playlist)
            .addParam(BR.adapter, mAdapter)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        mState.playlist.observe(viewLifecycleOwner) {
            launch {

            }
        }
        launch(Dispatchers.IO) {
            mState._playlist.postValue(
                dataBase.playlistDao().getById(args.playlistId)
            )
        }
    }
}