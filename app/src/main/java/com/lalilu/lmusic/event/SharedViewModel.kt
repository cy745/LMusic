package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.common.Mathf
import com.lalilu.lmusic.domain.request.AllPlayListRequest
import com.lalilu.lmusic.domain.request.NowPlaylistRequest
import com.lalilu.lmusic.domain.request.PageRequest
import com.lalilu.lmusic.utils.SharedPreferenceModule
import com.lalilu.lmusic.utils.SharedPreferenceModule.Companion.LAST_MUSIC_ID
import com.lalilu.lmusic.utils.SharedPreferenceModule.Companion.LAST_PLAYLIST_ID
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.entity.Music

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

    init {
        // 记录最后一次播放信息的 SharedPreference
        val lastPlaySp = SharedPreferenceModule.getInstance(null).lastPlaySp

        // 监听正在播放中的 playlistId
        nowPlaylistId.observeForever {
            // 如果 nowPlaylistId 的 value 为空则从 lastPlaySp 获取信息
            // 否则将其写入 lastPlaySp
            var playlistId = it
            if (playlistId == null) {
                playlistId = lastPlaySp.getLong(LAST_PLAYLIST_ID, 0)
            } else {
                lastPlaySp.edit().putLong(LAST_PLAYLIST_ID, it).apply()
            }

            // 根据 playlistId 更新指定的 playlist 到 nowPlaylistRequest
            nowPlaylistRequest.requestData(playlistId)
        }

        // 监听正在播放中的 music
        nowPlayingMusic.observeForever { music ->
            // 如果 music 为空，则从 lastPlaySp 中获取上一次播放的 musicId
            // 并向数据库请求该 musicId 对应的 music
            // 否则将 music 的 musicId 存入 lastPlaySp
            var nowMusic = music
            if (nowMusic == null) {
                val musicId = lastPlaySp.getLong(LAST_MUSIC_ID, 0)
                val database = LMusicMediaModule.getInstance(null).database
                nowMusic = database.musicDao().getMusicById(musicId)
                nowMusic?.let { nowPlayingMusic.postValue(nowMusic) }
            } else {
                lastPlaySp.edit().putLong(LAST_MUSIC_ID, music.musicId).apply()
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