package com.lalilu.lmusic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.lmusic.entity.Song

@Dao
interface SongDao {

    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = Song::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(song: Song)

    @Delete(entity = Song::class)
    fun delete(song: Song)

    @Query("DELETE FROM song")
    fun deleteAll()

    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song")
    fun getAllLiveData(): LiveData<List<Song>>

    @Query("SELECT * FROM song ORDER BY insertTime")
    fun getAllOrderByInsertTime(): List<Song>

    @Query("SELECT * FROM song ORDER BY lastPlayTime")
    fun getAllOrderByLastPlayTime(): List<Song>
}