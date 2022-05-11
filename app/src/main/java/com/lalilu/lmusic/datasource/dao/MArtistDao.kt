package com.lalilu.lmusic.datasource.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.lalilu.lmusic.datasource.entity.MArtist
import com.lalilu.lmusic.datasource.entity.MLyric

@Dao
interface MArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg artist: MArtist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(artists: List<MArtist>)

    @Delete(entity = MLyric::class)
    fun delete(vararg artist: MArtist)
}
