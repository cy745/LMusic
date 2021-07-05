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

class SharedViewModel : ViewModel() {
    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    val nowPlaylistId = MutableLiveData<Long>(null)
    val nowPlayingMusic = MutableLiveData<Music>(null)

    val nowPlaylistRequest = NowPlaylistRequest()
    val allPlaylistRequest = AllPlayListRequest()
    val pageRequest = PageRequest()

    init {
        val lastPlaySp = SharedPreferenceModule.getInstance(null).lastPlaySp

        nowPlaylistId.observeForever {
            val lastMusicId = if (it == null) {
                lastPlaySp.getLong(LAST_PLAYLIST_ID, 0)
            } else {
                lastPlaySp.edit().putLong(LAST_PLAYLIST_ID, it).apply()
                it
            }
            nowPlaylistRequest.requestData(lastMusicId)
        }
        nowPlayingMusic.observeForever { music ->
            val nowMusic = if (music == null) {
                val musicId = lastPlaySp.getLong(LAST_MUSIC_ID, 0)
                val database = LMusicMediaModule.getInstance(null).database
                val result = database.musicDao().getMusicById(musicId)
                result?.let { nowPlayingMusic.postValue(it) }
                result
            } else {
                lastPlaySp.edit().putLong(LAST_MUSIC_ID, music.musicId).apply()
                music
            }

            val result = listOrderChange(
                nowPlaylistRequest.getData().value, nowMusic
            ) ?: return@observeForever
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