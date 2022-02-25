package com.lalilu.lmusic.fragment

import android.view.View
import androidx.navigation.fragment.navArgs
import com.lalilu.BR
import com.lalilu.R
import com.lalilu.databinding.FragmentSongDetailBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.SongInPlaylist
import com.lalilu.lmusic.domain.entity.MPlaylist
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
        mState._song.postValue(
            mediaSource.getSongById(args.songId)
        )
        binding.songDetailAddSongToPlaylistButton.setOnClickListener {
            showSongToPlaylistPopupWindow(binding.root)
        }
    }

    /**
     * 显示用于选择歌单的PopupWindow，并传入所需展示的歌单列表，
     * 设置好回调获取被选中的歌单列表
     *
     * @param anchorView 给PopupWindow作为锚点的View
     */
    private fun showSongToPlaylistPopupWindow(anchorView: View) =
        launch(Dispatchers.IO) {
            val list = dataBase.playlistDao().getAll().toMutableList()
            withContext(Dispatchers.Main) {
                MyPopupWindow(requireActivity()) {
                    saveSongToPlaylist(it)
                }.apply {
                    setData(list)
                }.showAsDropDown(anchorView)
            }
        }

    /**
     * 将获取到的Playlist列表取其id作为参数，创建实体存入数据库中
     *
     * @param targetList 获取到的歌单列表
     */
    private fun saveSongToPlaylist(targetList: List<MPlaylist>) =
        launch(Dispatchers.IO) {
            targetList.forEach { playlist ->
                playlist.playlistId ?: return@forEach
                dataBase.songInPlaylistDao().save(
                    SongInPlaylist(
                        playlistId = playlist.playlistId,
                        songId = args.songId
                    )
                )
            }
        }
}