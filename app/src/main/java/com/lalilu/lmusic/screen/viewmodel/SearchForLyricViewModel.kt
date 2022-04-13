package com.lalilu.lmusic.screen.viewmodel

import android.text.TextUtils
import android.view.View
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.lalilu.R
import com.lalilu.databinding.FragmentSearchForLyricHeaderBinding
import com.lalilu.lmusic.apis.NeteaseDataSource
import com.lalilu.lmusic.apis.bean.SongSearchSong
import com.lalilu.lmusic.datasource.LMusicDataBase
import com.lalilu.lmusic.datasource.PersistLyric
import com.lalilu.lmusic.event.GlobalData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class SearchForLyricViewModel @Inject constructor(
    private val neteaseDataSource: NeteaseDataSource,
    private val dataBase: LMusicDataBase
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    fun getSongResult(
        binding: FragmentSearchForLyricHeaderBinding,
        keyword: String,
        items: SnapshotStateList<SongSearchSong>
    ) = launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            items.clear()
            binding.searchForLyricRefreshAndTipsButton.text =
                binding.root.context.getString(R.string.button_search_for_lyric_searching)
            binding.searchForLyricRefreshAndTipsButton.visibility = View.VISIBLE
        }
        flow {
            val response = neteaseDataSource.searchForSong(keyword)
            val results = response?.result?.songs ?: emptyList()
            emit(results)
        }.onEach {
            withContext(Dispatchers.Main) {
                if (it.isEmpty()) {
                    binding.searchForLyricRefreshAndTipsButton.text =
                        binding.root.context.getString(R.string.button_search_for_lyric_no_result)
                } else {
                    binding.searchForLyricRefreshAndTipsButton.text = ""
                    binding.searchForLyricRefreshAndTipsButton.visibility = View.GONE
                }
                items.addAll(it)
            }
        }.catch {
            withContext(Dispatchers.Main) {
                binding.searchForLyricRefreshAndTipsButton.text = "搜索失败"
                items.clear()
            }
        }.launchIn(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveSongLyric(
        songId: Long?,
        mediaId: String,
        toastTips: (String) -> Unit = {},
        success: () -> Unit = {}
    ) = launch(Dispatchers.IO) {
        flow {
            if (songId != null) emit(songId)
            else toastTips("未选择匹配歌曲")
        }.mapLatest {
            toastTips("开始获取歌词")
            neteaseDataSource.searchForLyric(it)
        }.mapLatest {
            val lyric = it?.lrc?.lyric
            val tlyric = it?.tlyric?.lyric

            if (!TextUtils.isEmpty(lyric)) Pair(lyric!!, tlyric) else {
                toastTips("选中歌曲无歌词")
                null
            }
        }.onEach {
            it ?: return@onEach
            dataBase.persistLyricDao().save(
                PersistLyric(
                    mediaId = mediaId,
                    lyric = it.first,
                    tlyric = it.second
                )
            )
            GlobalData.updateCurrentMediaItem(mediaId)
            toastTips("保存匹配歌词成功")
            withContext(Dispatchers.Main) {
                success()
            }
        }.catch {
            toastTips("保存失败")
        }.launchIn(this)
    }
}