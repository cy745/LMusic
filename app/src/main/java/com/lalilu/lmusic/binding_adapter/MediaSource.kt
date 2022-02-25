package com.lalilu.lmusic.binding_adapter

import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.domain.entity.MArtist
import com.lalilu.lmusic.domain.entity.MSong

interface MediaSource {
    fun getAllSongs(): List<MSong>
    fun getAllAlbums(): List<MAlbum>
    fun getAllArtists(): List<MArtist>

    fun getSongById(id: Long): MSong?
    fun getAlbumById(id: Long): MAlbum?
    fun getArtistById(id: Long): MArtist?

    fun getSongsBySongIds(ids: List<Long>): List<MSong>
    fun getSongsByAlbumId(id: Long): List<MSong>
    fun getSongsByArtistId(id: Long): List<MSong>
}
