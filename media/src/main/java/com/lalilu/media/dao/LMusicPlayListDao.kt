package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lalilu.media.entity.LMusicPlayList

@Dao
interface LMusicPlayListDao {
    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = LMusicPlayList::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(lMusicPlayList: LMusicPlayList)

    @Query("DELETE FROM lmusic_playlist")
    fun deleteAll()

    @Query("SELECT * FROM lmusic_playlist")
    fun getAll(): List<LMusicPlayList>

    @Query("SELECT * FROM lmusic_playlist")
    fun getAllLiveData(): LiveData<List<LMusicPlayList>>

    @Query("SELECT * FROM lmusic_playlist ORDER BY insertTime")
    fun getAllOrderByInsertTime(): List<LMusicPlayList>

    @Query("SELECT * FROM lmusic_playlist ORDER BY lastPlayTime")
    fun getAllOrderByLastPlayTime(): List<LMusicPlayList>
}