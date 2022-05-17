package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.MLyric

@Dao
interface MLyricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg lyric: MLyric)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(lyricList: List<MLyric>)

    @Query("SELECT * FROM network_lyric WHERE network_lyric_media_id = :id;")
    fun getById(id: String): MLyric?

    @Delete(entity = MLyric::class)
    fun delete(vararg lyric: MLyric)
}
