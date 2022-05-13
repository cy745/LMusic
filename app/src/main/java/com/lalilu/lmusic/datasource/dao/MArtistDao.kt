package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.ArtistMapIds
import com.lalilu.lmusic.datasource.entity.CustomMapArtists
import com.lalilu.lmusic.datasource.entity.MArtist
import com.lalilu.lmusic.datasource.entity.MArtistMapId
import kotlinx.coroutines.flow.Flow

@Dao
interface MArtistDao {

    /**
     * 获取顶级Artist
     */
    @Transaction
    @Query("SELECT * FROM m_artist WHERE map_to_artist IS NULL;")
    fun getAllArtistMapId(): Flow<List<ArtistMapIds>>

    /**
     * 获取该Artist所映射的所有Artist的原始ID映射
     */
    @Transaction
    fun getCustomMapArtists(artistName: String): ArtistMapIds {
        val mapArtists = getMapArtistsByName(artistName)
        return ArtistMapIds(mapArtists.artist, mapArtists.all.map { artist ->
            getArtistByName(artist.artistName).mapIds
        }.flatten())
    }

    /**
     * 根据[artistName]获取该Artist的原始ID映射
     */
    @Transaction
    @Query("SELECT * FROM m_artist WHERE artist_name = :artistName;")
    fun getArtistByName(artistName: String): ArtistMapIds

    /**
     * 获取用户自定义的 artist -> artists 映射
     */
    @Transaction
    @Query("SELECT * FROM m_artist WHERE artist_name = :artistName;")
    fun getMapArtistsByName(artistName: String): CustomMapArtists


    @Transaction
    fun saveArtist(artistName: String, originArtistId: String) {
        artistName.trim().trimEnd().takeIf { it.isNotEmpty() }?.let {
            save(MArtist(it))
            saveOriginIdMapping(MArtistMapId(it, originArtistId))
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(vararg artist: MArtist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(artists: List<MArtist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveOriginIdMapping(mapping: MArtistMapId)

    @Delete(entity = MArtist::class)
    fun delete(vararg artist: MArtist)
}
