package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.media.entity.LMusicMediaItem

@Dao
interface LMusicMediaItemDao {

    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = LMusicMediaItem::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(lMusicMediaItem: LMusicMediaItem)

    @Delete(entity = LMusicMediaItem::class)
    fun delete(lMusicMediaItem: LMusicMediaItem)

    @Query("DELETE FROM lmusic_media_item")
    fun deleteAll()

    @Query("SELECT * FROM lmusic_media_item")
    fun getAll(): List<LMusicMediaItem>

    @Query("SELECT * FROM lmusic_media_item")
    fun getAllLiveData(): LiveData<List<LMusicMediaItem>>

    @Query("SELECT * FROM lmusic_media_item ORDER BY insertTime")
    fun getAllOrderByInsertTime(): List<LMusicMediaItem>

    @Query("SELECT * FROM lmusic_media_item ORDER BY lastPlayTime")
    fun getAllOrderByLastPlayTime(): List<LMusicMediaItem>
}