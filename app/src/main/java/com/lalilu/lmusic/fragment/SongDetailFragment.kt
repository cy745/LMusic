package com.lalilu.lmusic.fragment

import androidx.navigation.fragment.navArgs
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentSongDetailBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.fragment.viewmodel.SongDetailViewModel
import com.lalilu.lmusic.ui.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class SongDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: SongDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: SongDetailViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_song_detail)
            .addParam(BR.vm, mState)
    }

    override fun onViewCreated() {
        val binding = mBinding as FragmentSongDetailBinding
        launch(Dispatchers.IO) {
            mState._song.postValue(
                mediaSource.getSongById(args.songId)
            )
        }
        binding.songDetailAddSongToPlaylistButton.setOnClickListener {
            launch(Dispatchers.IO) {
                val list = dataBase.playlistDao().getAll().toMutableList()
                withContext(Dispatchers.Main) {
                    MyPopupWindow(requireActivity()) {
                        println("checked: ${it.size}")
                    }.apply {
                        setData(list)
                    }.showAsDropDown(binding.root)
                }
            }
        }
    }
}