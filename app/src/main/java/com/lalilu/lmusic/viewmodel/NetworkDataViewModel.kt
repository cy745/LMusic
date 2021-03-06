package com.lalilu.lmusic.viewmodel

import android.text.TextUtils
import android.view.View
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.R
import com.lalilu.databinding.FragmentSearchForLyricHeaderBinding
import com.lalilu.lmusic.apis.NeteaseDataSource
import com.lalilu.lmusic.apis.bean.netease.SongSearchSong
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.entity.MNetworkData
import com.lalilu.lmusic.datasource.entity.MNetworkDataUpdateForCoverUrl
import com.lalilu.lmusic.datasource.entity.MNetworkDataUpdateForLyric
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NetworkDataViewModel @Inject constructor(
    private val neteaseDataSource: NeteaseDataSource,
    private val dataBase: MDataBase
) : ViewModel() {
    fun getNetworkDataFlowByMediaId(mediaId: String) =
        dataBase.networkDataDao().getFlowById(mediaId)
            .distinctUntilChanged()

    suspend fun getNetworkDataByMediaId(mediaId: String): MNetworkData? =
        withContext(Dispatchers.IO) {
            return@withContext dataBase.networkDataDao().getById(mediaId)
        }

    fun getSongResult(
        binding: FragmentSearchForLyricHeaderBinding,
        keyword: String,
        items: SnapshotStateList<SongSearchSong>
    ) = viewModelScope.safeLaunch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            items.clear()
            binding.searchForLyricRefreshAndTipsButton.text =
                binding.root.context.getString(R.string.button_match_network_data_searching)
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
                        binding.root.context.getString(R.string.button_match_network_data_no_result)
                } else {
                    binding.searchForLyricRefreshAndTipsButton.text = ""
                    binding.searchForLyricRefreshAndTipsButton.visibility = View.GONE
                }
                items.addAll(it)
            }
        }.catch {
            withContext(Dispatchers.Main) {
                binding.searchForLyricRefreshAndTipsButton.text = "????????????"
                items.clear()
            }
        }.launchIn(this)
    }

    fun saveMatchNetworkData(
        mediaId: String,
        songId: Long?,
        title: String?,
        success: () -> Unit = {}
    ) = viewModelScope.safeLaunch(Dispatchers.IO) {
        if (songId == null || title == null) {
            ToastUtils.showShort("?????????????????????")
            return@safeLaunch
        }
        try {
            dataBase.networkDataDao().save(
                MNetworkData(
                    mediaId = mediaId,
                    songId = songId.toString(),
                    title = title
                )
            )
            ToastUtils.showShort("????????????????????????")
            withContext(Dispatchers.Main) {
                success()
            }
        } catch (e: Exception) {
            ToastUtils.showShort("????????????????????????")
        }
    }

    fun saveCoverUrlIntoNetworkData(
        songId: String?,
        mediaId: String,
    ) = viewModelScope.safeLaunch(Dispatchers.IO) {
        if (songId == null) {
            ToastUtils.showShort("?????????????????????")
            return@safeLaunch
        }
        try {
            neteaseDataSource.searchForDetail(songId)?.let {
                dataBase.networkDataDao().updateCoverUrl(
                    MNetworkDataUpdateForCoverUrl(
                        mediaId = mediaId,
                        cover = it.songs[0].al.picUrl
                    )
                )
                ToastUtils.showShort("????????????????????????")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showShort("????????????????????????")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveLyricIntoNetworkData(
        songId: String?,
        mediaId: String,
        success: () -> Unit = {}
    ) = viewModelScope.safeLaunch(Dispatchers.IO) {
        flow {
            if (songId != null) emit(songId)
            else ToastUtils.showShort("?????????????????????")
        }.mapLatest {
            ToastUtils.showShort("??????????????????")
            neteaseDataSource.searchForLyric(it)
        }.mapLatest {
            val lyric = it?.mainLyric
            if (TextUtils.isEmpty(lyric)) {
                ToastUtils.showShort("?????????????????????")
                return@mapLatest null
            }
            Pair(lyric!!, it.translateLyric)
        }.onEach {
            it ?: return@onEach
            dataBase.networkDataDao().updateLyric(
                MNetworkDataUpdateForLyric(
                    mediaId = mediaId,
                    lyric = it.first,
                    tlyric = it.second
                )
            )
            ToastUtils.showShort("????????????????????????")
            withContext(Dispatchers.Main) {
                success()
            }
        }.catch {
            ToastUtils.showShort("????????????")
        }.launchIn(this)
    }
}