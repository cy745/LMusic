package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.database.MediaSource
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.fragment.viewmodel.AlbumDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: AlbumDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: AlbumDetailViewModel

    @Inject
    lateinit var mAdapter: ListAdapter

    @Inject
    lateinit var mediaSource: MediaSource

    @Inject
    lateinit var playerModule: PlayerModule

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = {
            playerModule.mediaController?.transportControls
                ?.playFromMediaId("${it.songId}", null)
        }
        return DataBindingConfig(R.layout.fragment_detail_album)
            .addParam(BR.adapter, mAdapter)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        mState.album.observe(viewLifecycleOwner) {
            launch {
                val list = mediaSource.getSongsByAlbumId(it?.albumId)
                withContext(Dispatchers.Main) {
                    mAdapter.setDiffNewData(list)
                }
            }
        }
        mState._album.postValue(
            MAlbum(args.albumId, args.albumTitle ?: "空")
        )
    }
}