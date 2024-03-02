package com.lalilu.lmusic.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.wrapper.Taglib
import com.lalilu.lmusic.api.lrcshare.LrcShareApi
import com.lalilu.lmusic.api.lrcshare.SongResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchLyricViewModel(
    private val context: Application,
    private val lrcShareApi: LrcShareApi
) : ViewModel() {
    enum class SearchState {
        Idle, Searching, Downloading, Error, Finished
    }

    val songResults = mutableStateListOf<SongResult>()
    val searchState = mutableStateOf(SearchState.Idle)
    val lastSearchMediaId = mutableStateOf("")

    fun searchFor(
        song: String,
        artist: String? = null,
        album: String? = null
    ) {
        if (searchState.value == SearchState.Searching) {
            ToastUtils.showShort("正在搜索，请稍候")
            return
        }

        if (song.isBlank()) {
            ToastUtils.showShort("请输入关键词")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                searchState.value = SearchState.Searching
                songResults.clear()

                val results = lrcShareApi.searchForSong(song, artist, album)

                songResults.addAll(results.distinctBy { it.id })
                searchState.value = SearchState.Finished
            } catch (e: Exception) {
                ToastUtils.showShort(e.message)
                searchState.value = SearchState.Error
            }
        }
    }

    fun saveLyricInto(lyricId: Int, mediaId: String, onSuccess: () -> Unit) {
        if (searchState.value == SearchState.Searching || searchState.value == SearchState.Downloading) {
            ToastUtils.showShort("正在处理，请稍候")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                searchState.value = SearchState.Downloading
                ToastUtils.showShort("开始获取歌词")

                val lyric = lrcShareApi.getLyricById(lyricId)?.lyric
                val song = LMedia.get<LSong>(mediaId)!!

                val result = context.contentResolver
                    .openFileDescriptor(song.uri, "rw")
                    .use {
                        it ?: return@use false

                        Taglib.writeLyricInto(it.detachFd(), lyric ?: "")
                    }

                if (!result) {
                    throw RuntimeException("保存失败")
                }

                searchState.value = SearchState.Idle
                ToastUtils.showShort("歌词保存成功")
                onSuccess()
            } catch (e: Exception) {
                ToastUtils.showShort(e.message)
            }
        }
    }
}