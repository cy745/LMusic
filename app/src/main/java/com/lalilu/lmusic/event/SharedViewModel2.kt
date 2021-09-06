package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.common.Mathf
import com.lalilu.lmusic.domain.request.*
import com.lalilu.media.entity.LPlaylist
import com.lalilu.media.entity.LSong
import com.tencent.mmkv.MMKV

/**
 *  为数据的全局同步而设置的 ViewModel ，即 mEvent，
 *  其生命周期属于 Application，在 Service 和 Activity 中都可以通过上下文获取到实例对象
 *
 */
class SharedViewModel2 : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    val nowPlaylistId = MutableLiveData<Long>(null)
    val nowPlayingId = MutableLiveData<Long>(null)
    val nowPlayingMusic = MutableLiveData<LSong>(null)

    val nowPlayingRequest = NowPlayingRequest2()
    val nowPlaylistRequest = NowPlaylistRequest2()
    val allPlaylistRequest = AllPlayListRequest2()
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
                mmkv.encode(LAST_PLAYLIST_ID, playlistId)
            }

            // 根据 playlistId 更新指定的 playlist 到 nowPlaylistRequest
            nowPlaylistRequest.requestData(playlistId)
        }

        // 监听正在播放中的 music
        nowPlayingId.observeForever { songId ->
            // 如果 music 为空，则从 mmkv 获取上一次播放的 musicId
            // 否则将 music 的 musicId 存入 mmkv 的映射区
            var nowSongId = songId

            if (nowSongId == null) {
                nowSongId = mmkv.decodeLong(LAST_MUSIC_ID, 0)
            } else {
                mmkv.encode(LAST_MUSIC_ID, nowSongId)
            }

            // 请求获取指定Id的Song
            nowPlayingRequest.requestData(nowSongId)

            // 根据正在播放的 song 改变 playlist 的顺序
            // 即正在播放的 song 处于 playlist 的首部
            val oldPlaylist = nowPlaylistRequest.getData().value ?: return@observeForever
            val nowPlayingSong = nowPlayingRequest.getData().value ?: return@observeForever
            val result = listOrderChange(oldPlaylist, nowPlayingSong) ?: return@observeForever
            nowPlaylistRequest.postData(result)
        }
    }

    private fun listOrderChange(oldList: LPlaylist, song: LSong): LPlaylist? {
        val nowPosition = oldList.songs?.indexOf(song)
        if (nowPosition == -1 || nowPosition == null) return null

        val songList = oldList.songs ?: return null
        oldList.songs = ArrayList(songList.map {
            val position =
                Mathf.clampInLoop(0, songList.size - 1, songList.indexOf(it), nowPosition)
            songList[position]
        })

        return oldList
    }
}