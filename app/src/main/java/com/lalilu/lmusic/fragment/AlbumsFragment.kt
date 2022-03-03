package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentListAlbumsBinding
import com.lalilu.lmusic.adapter.AlbumsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.ALBUM_ID
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.fragment.viewmodel.AlbumsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumsFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    @Inject
    lateinit var mState: AlbumsViewModel

    @Inject
    lateinit var mAdapter: AlbumsAdapter

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @SuppressLint("UnsafeOptInUsageError")
    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = {
            mState._position.postValue(requireScrollOffset())
            findNavController().navigate(
                AlbumsFragmentDirections.toAlbumDetail(
                    albumId = it.mediaId.toLong(),
                    albumTitle = it.mediaMetadata.albumTitle.toString()
                )
            )
        }
        mAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        return DataBindingConfig(R.layout.fragment_list_albums)
            .addParam(BR.albumsAdapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentListAlbumsBinding
        val recyclerView = binding.albumsRecyclerView
        mAdapter.setDiffNewData(
            mediaSource.getChildren(ALBUM_ID)?.toMutableList()
        )
        mState.position.observe(viewLifecycleOwner) {
            it ?: return@observe
            launch {
                delay(50)
                recyclerView.scrollToPosition(it)
            }
        }
    }

    private fun requireScrollOffset(): Int {
        if (mBinding == null || mBinding !is FragmentListAlbumsBinding) {
            return 0
        }
        return (mBinding as FragmentListAlbumsBinding)
            .albumsRecyclerView
            .computeVerticalScrollOffset()
    }
}