package com.lalilu.lmusic.event

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import com.lalilu.lmusic.domain.request.*
import com.lalilu.lmusic.utils.Mathf
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  为数据的全局同步而设置的 ViewModel ，即 mEvent，
 *  其生命周期属于 Application，在 Service 和 Activity 中都可以通过上下文获取到实例对象
 *
 */
class SharedViewModel : ViewModel() {
    private val database = LMusicDataBase.getInstance(null)

    val nowBgPalette: MutableLiveData<Palette> = MutableLiveData()

    val nowPlaylistId = MutableLiveData<Long>(null)
    val nowPlayingId = MutableLiveData<Long>(null)

    @Deprecated("nowMSongRequest 替代，后期删除")
    val nowPlayingRequest = NowPlayingRequest()

    @Deprecated("nowPlaylistWithSongsRequest 替代，后期删除")
    val nowPlaylistRequest = NowPlaylistRequest()

    val nowMSongRequest = NowMSongRequest()
    val nowPlaylistWithSongsRequest = NowPlaylistWithSongsRequest()

    val allPlaylistRequest = AllPlayListRequest()
    val pageRequest = PageRequest()
    val mmkv = MMKV.defaultMMKV()

    companion object {
        const val LAST_MUSIC_ID = "last_music_id"
        const val LAST_PLAYLIST_ID = "last_playlist_id"
    }

    private var playlistId = mmkv.decodeLong(LAST_PLAYLIST_ID, 0)
    private var songId = mmkv.decodeLong(LAST_MUSIC_ID, 0)

    init {
        // 监听正在播放的歌单的 playlistId
        nowPlaylistId.observeForever {
            playlistId = it ?: mmkv.decodeLong(LAST_PLAYLIST_ID, 0)
            mmkv.encode(LAST_PLAYLIST_ID, playlistId)
            nowPlaylistWithSongsRequest.requireData(playlistId)
        }

        // 监听正在播放的歌曲的 songId
        nowPlayingId.observeForever {
            GlobalScope.launch(Dispatchers.IO) {
                songId = it ?: mmkv.decodeLong(LAST_MUSIC_ID, 0)
                mmkv.encode(LAST_MUSIC_ID, songId)

                val song = database.songDao().getById(songId)
                nowMSongRequest.postData(song)

                val oldPlaylist = nowPlaylistWithSongsRequest.getData().value
                    ?: database.playlistDao().getById(playlistId) ?: return@launch
                val newPlayList = listOrderChange(oldPlaylist, song) ?: return@launch
                nowPlaylistWithSongsRequest.postData(newPlayList)
            }
        }
    }

    private fun listOrderChange(oldList: PlaylistWithSongs, newSong: MSong): PlaylistWithSongs? {
        val nowPosition = oldList.songs.indexOf(newSong)
        if (nowPosition == -1) return null

        val songList = oldList.songs
        oldList.songs = ArrayList(songList.map { song ->
            val position = Mathf.clampInLoop(
                0, songList.size - 1, songList.indexOf(song), nowPosition
            )
            songList[position]
        })

        return oldList
    }
}