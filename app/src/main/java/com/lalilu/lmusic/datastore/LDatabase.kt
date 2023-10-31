package com.lalilu.lmusic.datastore

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.utils.DateConverter
import com.lalilu.lmusic.utils.UriConverter

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