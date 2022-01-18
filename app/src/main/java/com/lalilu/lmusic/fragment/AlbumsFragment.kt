package com.lalilu.lmusic.fragment

import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.R
import com.lalilu.databinding.FragmentAlbumsBinding
import com.lalilu.lmusic.adapter.MSongAlbumsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.utils.GridItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AlbumsFragment : DataBindingFragment() {

    private var spanCount = 2
    private var gap = 10

    @Inject
    lateinit var mAdapter: MSongAlbumsAdapter

    @Inject
    lateinit var dataModule: DataModule

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var itemDecoration: GridItemDecoration

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.setOnItemClickListener { adapter, _, position ->
            val album = adapter.data[position] as MAlbum

            findNavController().navigate(
                AlbumsFragmentDirections.albumDetail(
                    albumId = album.albumId,
                    title = album.albumTitle
                )
            )
        }
        return DataBindingConfig(R.layout.fragment_albums)
            .addParam(BR.albumsAdapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = (mBinding as FragmentAlbumsBinding)
        val recyclerView = binding.albumsRecyclerView
        layoutManager = GridLayoutManager(requireContext(), spanCount)
        itemDecoration = GridItemDecoration(gap, spanCount)

        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(itemDecoration)

        dataModule.allAlbum.observe(viewLifecycleOwner) {
            mAdapter.setDiffNewData(it?.toMutableList())
        }
    }
}