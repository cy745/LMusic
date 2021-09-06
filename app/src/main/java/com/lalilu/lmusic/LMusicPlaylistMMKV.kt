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
        const val PLAYLIST_ALL = "all_playlist"
    }

    fun create(title: String, intro: String?, artUri: String) {
        val playlist = LPlaylist(title, intro, artUri)

        save(read().also {
            it.playlists.add(playlist)
        })
    }

    fun addSongToPlaylist(song: LSong, playlist: LPlaylist) {
        save(read().also {
            it.playlists[it.playlists.indexOf(playlist)].songs?.add(song)
        })
    }

    fun read(): LocalPlaylists {
        return mmkv.decodeParcelable(PLAYLIST_ALL, LocalPlaylists::class.java)
            ?: LocalPlaylists(ArrayList())
    }

    fun save(playlists: LocalPlaylists) {
        mmkv.encode(PLAYLIST_ALL, playlists)
    }

    @Parcelize
    data class LocalPlaylists(
        var playlists: ArrayList<LPlaylist>
    ) : Parcelable
}