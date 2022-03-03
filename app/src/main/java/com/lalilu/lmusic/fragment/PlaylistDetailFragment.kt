package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.library.baseAdapters.BR
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blankj.utilcode.util.KeyboardUtils
import com.lalilu.R
import com.lalilu.databinding.FragmentDetailPlaylistBinding
import com.lalilu.lmusic.adapter.ListAdapter
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.fragment.viewmodel.PlaylistDetailViewModel
import com.lalilu.lmusic.service.MSongBrowser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@SuppressLint("UnsafeOptInUsageError")
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
    lateinit var mSongBrowser: MSongBrowser

    override fun getDataBindingConfig(): DataBindingConfig {
        mAdapter.onItemClick = { item ->
            mSongBrowser.browser?.apply {
                clearMediaItems()
                setMediaItems(mAdapter.data)
                seekToDefaultPosition(mAdapter.data.indexOfFirst { it.mediaId == item.mediaId })
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        }
        mAdapter.onItemLongClick = {
            findNavController().navigate(
                PlaylistDetailFragmentDirections.playlistToSongDetail(it.mediaId.toLong())
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
                val songs = dataBase.songInPlaylistDao()
                    .getAllByPlaylistId(playlist.playlistId)
                    .mapNotNull {
                        mediaSource.getItemById(ITEM_PREFIX + it.songId)
                    }.toMutableList()
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