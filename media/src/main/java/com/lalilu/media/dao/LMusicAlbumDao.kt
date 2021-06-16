package com.lalilu.media.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lalilu.media.entity.LMusicAlbum


@Dao
interface LMusicAlbumDao {
    // onConflict 用于处理出现重复数据的情况
    @Insert(entity = LMusicAlbum::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: LMusicAlbum)

    @Delete(entity = LMusicAlbum::class)
    fun delete(album: LMusicAlbum)

    @Query("DELETE FROM lmusic_album")
    fun deleteAll()

    @Query("SELECT * FROM lmusic_album")
    fun getAllLiveData(): LiveData<List<LMusicAlbum>>
}