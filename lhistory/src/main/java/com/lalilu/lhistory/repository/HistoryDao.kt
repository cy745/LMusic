package com.lalilu.lhistory.repository

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lalilu.lhistory.entity.LHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(history: LHistory): Long

    @Update(entity = LHistory::class)
    fun update(vararg history: LHistory)

    @Query("UPDATE m_history SET duration = :duration, repeatCount = :repeatCount WHERE id = :id;")
    fun updateHistory(id: Long, duration: Long, repeatCount: Int)

    @Query("DELETE FROM m_history;")
    fun clear()

    @Delete(entity = LHistory::class)
    fun delete(vararg history: LHistory)

    @Query("SELECT * FROM m_history ORDER BY startTime DESC")
    fun getAllData(): PagingSource<Int, LHistory>

    @Query("SELECT * FROM m_history WHERE id = :id;")
    fun getById(id: Long): LHistory?

    /**
     * 查询播放历史，去除重复的记录，只保留最近的一条，按照最近播放时间排序
     */
    @Query(
        "SELECT * FROM " +
                "(SELECT id, contentId, contentTitle, parentId, parentTitle, duration, repeatCount, max(startTime) as 'startTime' FROM m_history GROUP BY contentId) as A " +
                "ORDER BY A.startTime DESC LIMIT :limit;"
    )
    fun getFlow(limit: Int): Flow<List<LHistory>>

    /**
     * 查询播放历史，按照最近播放时间排序且计算每首歌的播放次数
     */
    @MapInfo(valueColumn = "count")
    @Query(
        "SELECT * FROM " +
                "(SELECT id, contentId, contentTitle, parentId, parentTitle, duration, repeatCount, (count(contentId) + repeatCount) as 'count', max(startTime) as 'startTime' FROM m_history GROUP BY contentId) as A " +
                "ORDER BY A.startTime DESC LIMIT :limit;"
    )
    fun getFlowWithCount(limit: Int): Flow<Map<LHistory, Int>>

    @MapInfo(keyColumn = "contentId", valueColumn = "count")
    @Query(
        "SELECT contentId, (count(contentId) + repeatCount) as 'count' FROM m_history GROUP BY contentId " +
                "LIMIT :limit;"
    )
    fun getFlowIdsMapWithCount(limit: Int): Flow<Map<String, Int>>

    @MapInfo(keyColumn = "contentId", valueColumn = "startTime")
    @Query(
        "SELECT contentId, max(startTime) as 'startTime' FROM m_history GROUP BY contentId " +
                "LIMIT :limit;"
    )
    fun getFlowIdsMapWithLastTime(limit: Int): Flow<Map<String, Long>>
}