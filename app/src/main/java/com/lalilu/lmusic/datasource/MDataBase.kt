package com.lalilu.lmusic.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.datasource.converter.DateConverter
import com.lalilu.lmusic.datasource.converter.UriConverter
import com.lalilu.lmusic.datasource.dao.MNetworkDataDao
import com.lalilu.lmusic.datasource.entity.MNetworkData

@Database(
    entities = [
        MNetworkData::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class MDataBase : RoomDatabase() {
    abstract fun networkDataDao(): MNetworkDataDao
}
