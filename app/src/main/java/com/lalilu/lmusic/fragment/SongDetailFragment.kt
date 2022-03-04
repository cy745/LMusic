package com.lalilu.lmusic.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.navigation.fragment.navArgs
import com.lalilu.R
import com.lalilu.databinding.FragmentSongDetailBinding
import com.lalilu.lmusic.base.DataBindingConfig
import com.lalilu.lmusic.base.DataBindingFragment
import com.lalilu.lmusic.binding_adapter.setCoverSourceUri
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.SongInPlaylist
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.fragment.viewmodel.SongDetailViewModel
import com.lalilu.lmusic.service.MSongBrowser
import com.lalilu.lmusic.ui.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SongDetailFragment : DataBindingFragment(), CoroutineScope {
    private val args: SongDetailFragmentArgs by navArgs()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var mState: SongDetailViewModel

    @Inject
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var mSongBrowser: MSongBrowser

    @Inject
    lateinit var dataBase: LMusicDataBase

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_song_detail)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onViewCreated() {
        val binding = mBinding as FragmentSongDetailBinding
        mState._song.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.songDetailTitle.text = it.mediaMetadata.title.toString()
            binding.songDetailArtist.text = it.mediaMetadata.artist.toString()
            binding.detailCover.setCoverSourceUri(it.mediaMetadata.mediaUri)
        }
        mState._song.postValue(
            mediaSource.getItemById(ITEM_PREFIX + args.mediaId)
        )
        binding.songDetailAddSongToPlaylistButton.setOnClickListener {
            showSongToPlaylistPopupWindow(binding.root)
        }
        binding.songDetailSetSongToNextButton.setOnClickListener {
            mSongBrowser.addToNext(args.mediaId)
        }
        binding.songDetailSearchForLyricButton.setOnClickListener {

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
                dataBase.songInPlaylistDao().save(
                    SongInPlaylist(
                        playlistId = playlist.playlistId,
                        mediaId = args.mediaId
                    )
                )
            }
        }
}