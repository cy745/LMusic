package com.lalilu.lmusic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.lmusic.entity.Album

@Dao
interface AlbumDao {
    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = Album::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album)

    @Delete(entity = Album::class)
    fun delete(album: Album)

    @Query("DELETE FROM album")
    fun deleteAll()

    @Query("SELECT * FROM album")
    fun getAllLiveData(): LiveData<List<Album>>
}