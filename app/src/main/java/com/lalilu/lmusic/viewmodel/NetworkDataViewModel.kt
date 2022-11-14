package com.lalilu.lmusic.viewmodel

import android.text.TextUtils
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.lmedia.entity.LNetData
import com.lalilu.lmedia.entity.LNetDataUpdateForCoverUrl
import com.lalilu.lmedia.entity.LNetDataUpdateForLyric
import com.lalilu.lmedia.repository.NetDataRepository
import com.lalilu.lmusic.apis.*
import com.lalilu.lmusic.repository.LyricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NetworkDataViewModel @Inject constructor(
    private val neteaseDataSource: NeteaseDataSource,
    private val kugouDataSource: KugouDataSource,
    private val lyricRepository: LyricRepository,
    private val netDataRepo: NetDataRepository
) : ViewModel() {
    fun getNetworkDataFlowByMediaId(mediaId: String) = netDataRepo
        .getNetDataFlowById(mediaId)
        .distinctUntilChanged()

    fun getSongResult(
        keyword: String,
        items: SnapshotStateList<NetworkSong>,
        msg: MutableState<String>
    ) = viewModelScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            items.clear()
            msg.value = "搜索中..."
        }
        flow {
            kotlin.runCatching { neteaseDataSource.searchForSongs(keyword)?.songs }
                .getOrNull()
                ?.let { emit(it) }

            kotlin.runCatching { kugouDataSource.searchForSongs(keyword)?.songs }
                .getOrNull()
                ?.let { emit(it) }
        }.onEach {
            withContext(Dispatchers.Main) {
                msg.value = if (it.isEmpty()) "无结果" else ""
                items.addAll(it)
            }
        }.catch {
            println(it.message)
            withContext(Dispatchers.Main) {
                msg.value = "搜索失败"
                items.clear()
            }
        }.launchIn(this)
    }

    /**
     * 将选中的NetworkSong保存到数据库中
     *
     * @param mediaId       歌曲在本地媒体数据库中的ID
     * @param networkSong   云端请求到的歌曲数据信息
     * @param success       保存成功回调
     */
    fun saveMatchNetworkData(
        mediaId: String,
        networkSong: NetworkSong?,
        success: () -> Unit = {}
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (networkSong == null) {
            ToastUtils.showShort("未选择匹配歌曲")
            return@launch
        }
        try {
            netDataRepo.saveNetData(
                LNetData(
                    mediaId = mediaId,
                    netId = networkSong.songId,
                    title = networkSong.songTitle,
                    platform = networkSong.fromPlatform
                )
            )
            ToastUtils.showShort("保存匹配信息成功")
            withContext(Dispatchers.Main) { success() }
        } catch (e: Exception) {
            ToastUtils.showShort("保存匹配信息失败")
        }
    }

    /**
     * 根据获取到的云端ID进行封面地址的查询获取
     *
     * @param songId    歌曲的云端ID
     * @param mediaId   歌曲在本地媒体数据库中的ID
     */
    fun saveCoverIntoNetworkData(
        songId: String?,
        mediaId: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (songId == null) {
            ToastUtils.showShort("未选择匹配歌曲")
            return@launch
        }
        try {
            neteaseDataSource.searchForDetail(songId)?.let {
                netDataRepo.updateCoverUrl(
                    LNetDataUpdateForCoverUrl(
                        mediaId = mediaId,
                        cover = it.songs[0].al.picUrl
                    )
                )
                ToastUtils.showShort("保存封面地址成功")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showShort("保存封面地址失败")
        }
    }

    /**
     * 根据获取到的云端ID进行歌词的查询获取
     *
     * @param songId    歌曲的云端ID
     * @param mediaId   歌曲在本地媒体数据库中的ID
     * @param platform  请求接口的目标平台
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun saveLyricIntoNetworkData(
        songId: String?,
        mediaId: String,
        platform: Int?,
        success: () -> Unit = {}
    ) = viewModelScope.launch(Dispatchers.IO) {
        flow {
            if (songId != null) emit(songId)
            else ToastUtils.showShort("未选择匹配歌曲")
        }.mapLatest {
            ToastUtils.showShort("开始获取歌词")
            when (platform) {
                PLATFORM_NETEASE -> neteaseDataSource.searchForLyric(it)
                PLATFORM_KUGOU -> kugouDataSource.searchForLyric(it)
                else -> null
            }
        }.mapLatest {
            val lyric = it?.mainLyric
            if (TextUtils.isEmpty(lyric)) {
                ToastUtils.showShort("选中歌曲无歌词")
                return@mapLatest null
            }
            Pair(lyric!!, it.translateLyric)
        }.onEach {
            it ?: return@onEach
            netDataRepo.updateLyric(
                LNetDataUpdateForLyric(
                    mediaId = mediaId,
                    lyric = it.first,
                    tlyric = it.second
                )
            )
            ToastUtils.showShort("保存匹配歌词成功")
            lyricRepository.currentLyric.requireUpdate()
            withContext(Dispatchers.Main) {
                success()
            }
        }.catch { ToastUtils.showShort("保存失败") }
            .launchIn(this)
    }
}