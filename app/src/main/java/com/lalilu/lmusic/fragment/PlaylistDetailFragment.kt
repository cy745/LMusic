package com.lalilu.lmusic.fragment

import android.widget.TextView
import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.databinding.FragmentDetailPlaylistBinding
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.event.PlayerModule
import com.lalilu.lmusic.fragment.viewmodel.PlaylistDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
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
        mState.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.playlistId ?: return@observe

            launch(Dispatchers.IO) {
                val list = dataBase.songInPlaylistDao()
                    .getAllByPlaylistId(playlist.playlistId)
                    .map { it.songId }
                val songs = mediaSource.getSongsBySongIds(list)
                    .toMutableList()
                withContext(Dispatchers.Main) {
                    mAdapter.setDiffNewData(songs)
                }
            }
        }
        launch(Dispatchers.IO) {
            mState._playlist.postValue(
                dataBase.playlistDao().getById(args.playlistId)
            )
        }
        val binding = mBinding as FragmentDetailPlaylistBinding
        val callback = TextView.OnEditorActionListener { view, _, _ ->
            mState.playlist.value?.copy(
                playlistTitle = binding.playlistDetailTitle.text.toString(),
                playlistInfo = binding.playlistDetailInfo.text.toString()
            )?.let {
                launch(Dispatchers.IO) {
                    dataBase.playlistDao().update(it)
                }
            }
            view.clearFocus()
            KeyboardUtils.hideSoftInput(view)
            return@OnEditorActionListener true
        }
        binding.playlistDetailTitle.setOnEditorActionListener(callback)
        binding.playlistDetailInfo.setOnEditorActionListener(callback)
        KeyboardUtils.registerSoftInputChangedListener(requireActivity()) {
            if (it > 0) return@registerSoftInputChangedListener
            when {
                binding.playlistDetailTitle.isFocused ->
                    binding.playlistDetailTitle.onEditorAction(0)
                binding.playlistDetailInfo.isFocused ->
                    binding.playlistDetailInfo.onEditorAction(0)
            }
        }
    }
}