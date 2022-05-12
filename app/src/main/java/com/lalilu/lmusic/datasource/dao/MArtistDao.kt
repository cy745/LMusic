package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.ArtistMapIds
import com.lalilu.lmusic.datasource.entity.MArtist
import com.lalilu.lmusic.datasource.entity.MArtistMapId
import kotlinx.coroutines.flow.Flow

@Dao
interface MArtistDao {

    @Transaction
    @Query("SELECT * FROM m_artist WHERE map_to_artist IS NULL;")
    fun getAllArtistMapId(): Flow<List<ArtistMapIds>>

    @Transaction
    @Query("SELECT * FROM m_artist WHERE artist_name = :artistName;")
    fun getArtistByName(artistName: String): ArtistMapIds

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
