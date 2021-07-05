package com.lalilu.media.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lalilu.media.entity.Music

@Dao
interface MusicDao {
    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = Music::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(music: Music)

    @Query("DELETE FROM music")
    fun deleteAll()

    @Query("SELECT musicId FROM music")
    fun getAllId(): List<Long>?

    @Query("SELECT * FROM music")
    fun getAll(): List<Music>

    @Query("SELECT * FROM music WHERE musicId = (:id)")
    fun getMusicById(id: Long?): Music?

    @Query("SELECT * FROM music WHERE musicId = (:id)")
    fun getMusicById(id: String?): Music?

    @Query("SELECT musicUri FROM music WHERE musicId = (:id)")
    fun getUriById(id: Long): Uri?

    @Query("SELECT musicUri FROM music WHERE musicId = (:id)")
    fun getUriById(id: String): Uri?
}