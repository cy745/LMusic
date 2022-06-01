package com.lalilu.lmusic.datasource.dao

import androidx.room.*
import com.lalilu.lmusic.datasource.entity.MNetworkData
import com.lalilu.lmusic.datasource.entity.MNetworkDataUpdateForLyric
import kotlinx.coroutines.flow.Flow

@Dao
interface MNetworkDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg networkData: MNetworkData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(networkDataList: List<MNetworkData>)

    @Update(entity = MNetworkData::class)
    fun updateLyric(vararg mNetworkDataUpdateForLyric: MNetworkDataUpdateForLyric)

    @Query("SELECT * FROM network_data WHERE network_data_media_id = :id;")
    fun getById(id: String): MNetworkData?

    @Query("SELECT * FROM network_data WHERE network_data_media_id = :id;")
    fun getFlowById(id: String): Flow<MNetworkData?>

    @Delete(entity = MNetworkData::class)
    fun delete(vararg networkData: MNetworkData)
}
