package com.lalilu.lmusic

import android.os.Parcelable
import com.lalilu.lmusic.utils.SnowFlakeUtils
import com.lalilu.lmusic.domain.entity.LPlaylist
import com.lalilu.lmusic.domain.entity.LSong
import com.tencent.mmkv.MMKV
import kotlinx.parcelize.Parcelize

/**
 * 用MMKV实现的歌单存储方法
 */
class LMusicPlaylistMMKV private constructor() {
    private val mmkv = MMKV.defaultMMKV()

    companion object {
        const val PLAYLIST_LOCAL = "playlist_local"
        const val PLAYLIST_LOCAL_TITLE = "本地歌曲"
        const val PLAYLIST_LOCAL_ID = -1L

        const val PLAYLISTS_ALL = "all_playlist"

        @Volatile
        private var instance: LMusicPlaylistMMKV? = null

        fun getInstance(): LMusicPlaylistMMKV {
            instance ?: synchronized(LMusicPlaylistMMKV::class.java) {
                instance = instance ?: LMusicPlaylistMMKV()
            }
            return instance!!
        }
    }

    /**
     * 读取本地歌曲歌单
     */
    fun readLocalPlaylist(): LPlaylist {
        return mmkv.decodeParcelable(PLAYLIST_LOCAL, LPlaylist::class.java)
            ?: LPlaylist(PLAYLIST_LOCAL_ID, PLAYLIST_LOCAL_TITLE)
    }

    /**
     * 通过Id读取歌曲
     */
    fun readLocalSongById(id: Long): LSong? {
        readLocalPlaylist().songs?.let { songs ->
            val index = songs.indexOf(LSong(id, ""))
            if (index == -1) return null
            return songs[index]
        }
        return null
    }

    fun deleteAllLocalSong() {
        saveLocalPlaylist(readLocalPlaylist().also {
            it.songs?.clear()
            it.songs = ArrayList()
            println("clear完成, size: ${it.songs?.size}")
        })
    }

    /**
     * 通过Id删除歌曲
     */
    fun deleteLocalSongById(id: Long) {
        saveLocalPlaylist(readLocalPlaylist().also {
            it.songs?.remove(LSong(id, ""))
        })
    }

    /**
     * 保存歌曲到本地歌单中
     */
    fun saveSongToLocalPlaylist(song: LSong) {
        saveLocalPlaylist(readLocalPlaylist().also {
            if (it.songs?.indexOf(song) == -1) {
                println(song.mTitle)
                it.songs?.add(song)
            }
        })
    }

    /**
     * 保存本地歌曲歌单
     */
    fun saveLocalPlaylist(playlist: LPlaylist) {
        mmkv.encode(PLAYLIST_LOCAL, playlist)
    }

    /**
     * 创建新的歌单
     */
    fun createPlaylist(title: String, intro: String?, artUri: String) {
        val playlist = LPlaylist(SnowFlakeUtils.getFlowIdInstance().nextId(), title, intro, artUri)

        saveAllPlaylist(readAllPlaylist().also {
            it.playlists.add(playlist)
        })
    }

    /**
     * 通过歌单Id获取歌单
     */
    fun readPlaylistById(id: Long): LPlaylist? {
        readAllPlaylist().also {
            val index = it.playlists.indexOf(LPlaylist(id, ""))
            if (index == -1) return null
            return it.playlists[index]
        }
    }

    /**
     * 通过歌单Id删除歌单
     */
    fun deletePlaylistById(id: Long) {
        saveAllPlaylist(readAllPlaylist().also {
            it.playlists.remove(LPlaylist(id, ""))
        })
    }

    /**
     * 添加歌曲到指定歌单中
     */
    fun addSongToPlaylist(song: LSong, playlist: LPlaylist) {
        saveAllPlaylist(readAllPlaylist().also {
            it.playlists[it.playlists.indexOf(playlist)].songs?.add(song)
        })
    }

    /**
     * 读取所有自创建歌单 (本地歌单除外)
     */
    fun readAllPlaylist(): LocalPlaylists {
        return mmkv.decodeParcelable(PLAYLISTS_ALL, LocalPlaylists::class.java)
            ?: LocalPlaylists(ArrayList())
    }

    /**
     * 保存自创建歌单
     */
    fun saveAllPlaylist(playlists: LocalPlaylists) {
        mmkv.encode(PLAYLISTS_ALL, playlists)
    }

    @Parcelize
    data class LocalPlaylists(
        var playlists: ArrayList<LPlaylist>
    ) : Parcelable
}