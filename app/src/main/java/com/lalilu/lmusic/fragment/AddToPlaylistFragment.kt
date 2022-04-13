package com.lalilu.lmusic.fragment

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentAddToPlaylistBinding
import com.lalilu.lmusic.adapter.AddSongToPlaylistsAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.viewmodel.AddToPlaylistViewModel
import com.lalilu.lmusic.viewmodel.bindViewModel
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
//    private val args: AddToPlaylistFragmentArgs by navArgs()

    @Inject
    lateinit var mState: AddToPlaylistViewModel

    @Inject
    lateinit var mAdapter: AddSongToPlaylistsAdapter

    @Inject
    lateinit var dataBase: LMusicDataBase

    private val defaultTitle: String by lazy {
        requireContext().resources.getString(R.string.destination_label_add_song_to_playlist)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.bindViewModel(mState, viewLifecycleOwner)
        mAdapter.onItemClick = { item, position ->
            val checkedSet = mAdapter.selectedSet
            if (checkedSet.contains(item)) checkedSet.remove(item)
            else checkedSet.add(item)
            mAdapter.notifyItemChanged(position)

            mState.title.postValue(
                if (checkedSet.size == 0) defaultTitle
                else "已选中: ${checkedSet.size}"
            )
        }
        return DataBindingConfig(R.layout.fragment_add_to_playlist)
            .addParam(BR.adapter, mAdapter)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentAddToPlaylistBinding
        binding.addToPlaylistCancel.setOnClickListener {
            mAdapter.selectedSet.clear()
            findNavController().navigateUp()
        }
        binding.addToPlaylistConfirm.setOnClickListener {
            launch(Dispatchers.IO) {
//                dataBase.songInPlaylistDao().save(
//                    mAdapter.selectedSet.map { playlist ->
//                        SongInPlaylist(
//                            playlistId = playlist.playlistId,
//                            mediaId = args.mediaId
//                        )
//                    }
//                )
                mAdapter.selectedSet.clear()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show()
                    try {
                        findNavController().navigateUp()
                    } catch (_: Exception) {
                    }
                }
            }
        }
        mState.title.postValue(defaultTitle)
        dataBase.playlistDao().getAllLiveDataSortByTime().observe(viewLifecycleOwner) {
            mState.postData(it)
        }
    }
}