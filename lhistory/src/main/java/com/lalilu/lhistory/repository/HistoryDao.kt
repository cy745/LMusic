package com.lalilu.lhistory.repository

import androidx.room.*
import com.lalilu.lhistory.entity.LHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg history: LHistory)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(history: List<LHistory>)

    @Update(entity = LHistory::class)
    fun update(vararg history: LHistory)

    @Query("UPDATE m_history SET duration = :duration WHERE contentId = :contentId AND duration = -1;")
    fun updatePreSavedHistory(contentId: String, duration: Long)

    @Query("DELETE FROM m_history WHERE contentId = :contentId AND duration = -1;")
    fun deletePreSavedHistory(contentId: String)

    @Query("DELETE FROM m_history;")
    fun clear()

    @Delete(entity = LHistory::class)
    fun delete(vararg history: LHistory)

    @Query("SELECT * FROM m_history;")
    fun getAll(): List<LHistory>

    @Query("SELECT * FROM m_history WHERE id = :id;")
    fun getById(id: Long): LHistory?

    /**
     * 查询播放历史，去除重复的记录，只保留最近的一条，按照最近播放时间排序
     */
    @Query(
        "SELECT * FROM " +
                "(SELECT id, contentId, duration, type, max(startTime) as 'startTime' FROM m_history GROUP BY contentId) as A " +
                "ORDER BY A.startTime DESC LIMIT :limit;"
    )
    fun getFlow(limit: Int): Flow<List<LHistory>>

    /**
     * 查询播放历史，按照最近播放时间排序且计算每首歌的播放次数
     */
    @MapInfo(valueColumn = "count")
    @Query(
        "SELECT * FROM " +
                "(SELECT id, contentId, duration, type, count(contentId) as 'count', max(startTime) as 'startTime' FROM m_history GROUP BY contentId) as A " +
                "ORDER BY A.startTime DESC LIMIT :limit;"
    )
    fun getFlowWithCount(limit: Int): Flow<Map<LHistory, Int>>

    @MapInfo(keyColumn = "contentId", valueColumn = "count")
    @Query(
        "SELECT contentId, count(contentId) as 'count' FROM m_history GROUP BY contentId " +
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