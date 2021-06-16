package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.media.entity.LMusicMedia

@Dao
interface LMusicMediaDao {

    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = LMusicMedia::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(lMusicMedia: LMusicMedia)

    @Delete(entity = LMusicMedia::class)
    fun delete(lMusicMedia: LMusicMedia)

    @Query("DELETE FROM lmusic_media")
    fun deleteAll()

    @Query("SELECT * FROM lmusic_media")
    fun getAll(): List<LMusicMedia>

    @Query("SELECT * FROM lmusic_media")
    fun getAllLiveData(): LiveData<List<LMusicMedia>>

    @Query("SELECT * FROM lmusic_media ORDER BY insertTime")
    fun getAllOrderByInsertTime(): List<LMusicMedia>

    @Query("SELECT * FROM lmusic_media ORDER BY lastPlayTime")
    fun getAllOrderByLastPlayTime(): List<LMusicMedia>
}