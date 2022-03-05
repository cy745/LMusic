package com.lalilu.lmusic.fragment

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentAddToPlaylistBinding
import com.lalilu.lmusic.adapter.AddSongToPlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.SongInPlaylist
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class AddToPlaylistFragment : DataBindingFragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val args: AddToPlaylistFragmentArgs by navArgs()

    @Inject
    lateinit var mAdapter: AddSongToPlaylistsAdapter

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        return DataBindingConfig(R.layout.fragment_add_to_playlist)
            .addParam(BR.adapter, mAdapter)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentAddToPlaylistBinding
        binding.addToPlaylistCancel.setOnClickListener {
            mAdapter.selectedSet.clear()
            findNavController().navigateUp()
        }
        binding.addToPlaylistConfirm.setOnClickListener {
            launch(Dispatchers.IO) {
                dataBase.songInPlaylistDao().save(
                    mAdapter.selectedSet.map { playlist ->
                        SongInPlaylist(
                            playlistId = playlist.playlistId,
                            mediaId = args.mediaId
                        )
                    }
                )
                mAdapter.selectedSet.clear()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
        mAdapter.onItemClick = {
            val index = mAdapter.data.indexOf(it)
            val checkedSet = mAdapter.selectedSet
            if (checkedSet.contains(it)) checkedSet.remove(it)
            else checkedSet.add(it)
            mAdapter.notifyItemChanged(index)

            binding.addToPlaylistTips.text = if (checkedSet.size == 0) "添加至歌单"
            else "已选中: ${checkedSet.size}"
        }
        dataBase.playlistDao().getAllLiveDataSortByTime().observe(viewLifecycleOwner) {
            println(it.size)
            mAdapter.setDiffNewData(it.toMutableList())
            println(mAdapter.data.size)
        }
    }
}