package com.lalilu.lmusic

import android.os.Parcelable
import com.lalilu.media.entity.LPlaylist
import com.lalilu.media.entity.LSong
import com.tencent.mmkv.MMKV
import kotlinx.parcelize.Parcelize

/**
 * 用MMKV实现的歌单存储方法
 */
class LMusicPlaylistMMKV {
    private val mmkv = MMKV.defaultMMKV()

    companion object {
        const val PLAYLIST_LOCAL = "playlist_local"
        const val PLAYLIST_LOCAL_TITLE = "本地歌曲"

        const val PLAYLISTS_ALL = "all_playlist"
    }

    /**
     * 读取本地歌曲歌单
     */
    fun readLocal(): LPlaylist {
        return mmkv.decodeParcelable(PLAYLIST_LOCAL, LPlaylist::class.java)
            ?: LPlaylist(PLAYLIST_LOCAL_TITLE)
    }

    fun saveSongToLocal(song: LSong) {
        saveLocal(readLocal().also {
            if (it.songs?.indexOf(song) == -1) {
                it.songs?.add(song)
            }
        })
    }

    /**
     * 保存本地歌曲歌单
     */
    fun saveLocal(playlist: LPlaylist) {
        mmkv.encode(PLAYLIST_LOCAL, playlist)
    }

    /**
     * 创建新的歌单
     */
    fun create(title: String, intro: String?, artUri: String) {
        val playlist = LPlaylist(title, intro, artUri)

        save(read().also {
            it.playlists.add(playlist)
        })
    }

    /**
     * 添加歌曲到指定歌单中
     */
    fun addSongToPlaylist(song: LSong, playlist: LPlaylist) {
        save(read().also {
            it.playlists[it.playlists.indexOf(playlist)].songs?.add(song)
        })
    }

    /**
     * 读取所有自创建歌单 (本地歌单除外)
     */
    fun read(): LocalPlaylists {
        return mmkv.decodeParcelable(PLAYLISTS_ALL, LocalPlaylists::class.java)
            ?: LocalPlaylists(ArrayList())
    }

    /**
     * 保存自创建歌单
     */
    fun save(playlists: LocalPlaylists) {
        mmkv.encode(PLAYLISTS_ALL, playlists)
    }

    @Parcelize
    data class LocalPlaylists(
        var playlists: ArrayList<LPlaylist>
    ) : Parcelable
}