package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.common.Mathf
import com.lalilu.lmusic.domain.request.AllPlayListRequest
import com.lalilu.lmusic.domain.request.NowPlaylistRequest
import com.lalilu.lmusic.domain.request.PageRequest
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music
import com.tencent.mmkv.MMKV

/**
 *  为数据的全局同步而设置的 ViewModel ，即 mEvent，
 *  其生命周期属于 Application，在 Service 和 Activity 中都可以通过上下文获取到实例对象
 *
 */
class SharedViewModel : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    val nowPlaylistId = MutableLiveData<Long>(null)
    val nowPlayingMusic = MutableLiveData<Music>(null)

    val nowPlaylistRequest = NowPlaylistRequest()
    val allPlaylistRequest = AllPlayListRequest()
    val pageRequest = PageRequest()
    val mmkv = MMKV.defaultMMKV()

    companion object {
        const val LAST_MUSIC_ID = "last_music_id"
        const val LAST_PLAYLIST_ID = "last_playlist_id"
    }

    init {
        // 监听正在播放中的 playlistId
        nowPlaylistId.observeForever {
            // 如果 nowPlaylistId 的 value 为空则从 mmkv 获取信息
            // 否则将其写入 mmkv 的映射区
            var playlistId = it
            if (playlistId == null) {
                playlistId = mmkv.decodeLong(LAST_PLAYLIST_ID, 0)
            } else {
                mmkv.encode(LAST_PLAYLIST_ID, it)
            }

            // 根据 playlistId 更新指定的 playlist 到 nowPlaylistRequest
            nowPlaylistRequest.requestData(playlistId)
        }

        // 监听正在播放中的 music
        nowPlayingMusic.observeForever { music ->
            // 如果 music 为空，则从 mmkv 获取上一次播放的 musicId
            // 并向数据库请求该 musicId 对应的 music
            // 否则将 music 的 musicId 存入 mmkv 的映射区
            var nowMusic = music
            if (nowMusic == null) {
                val musicId = mmkv.decodeLong(LAST_MUSIC_ID, 0)
                val database = LMusicMediaModule.getInstance(null).database
                nowMusic = database.musicDao().getMusicById(musicId)
                nowMusic?.let { nowPlayingMusic.postValue(nowMusic) }
            } else {
                mmkv.encode(LAST_MUSIC_ID, music.musicId)
            }

            // 根据正在播放的 music 改变 playlist 的顺序
            // 即正在播放的 music 处于 playlist 的首部
            val oldPlaylist = nowPlaylistRequest.getData().value ?: return@observeForever
            val result = listOrderChange(oldPlaylist, nowMusic) ?: return@observeForever
            nowPlaylistRequest.postData(result)
        }
    }

    private fun listOrderChange(oldList: List<Music>?, music: Music?): List<Music>? {
        music ?: return null
        oldList ?: return null
        val nowPosition = oldList.indexOf(music)
        if (nowPosition == -1) return null
        return oldList.map {
            val position = Mathf.clampInLoop(0, oldList.size - 1, oldList.indexOf(it), nowPosition)
            oldList[position]
        }
    }
}