package com.lalilu.lhistory.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lhistory.entity.LHistory
import com.lalilu.lhistory.utils.DateConverter
import com.lalilu.lhistory.utils.UriConverter

@Database(
    version = 1,
    exportSchema = true,
    entities = [LHistory::class],
    autoMigrations = []
)
@TypeConverters(
    DateConverter::class,
    UriConverter::class,
)
abstract class LDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}