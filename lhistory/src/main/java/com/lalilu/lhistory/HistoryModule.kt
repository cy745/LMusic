package com.lalilu.lhistory

import android.app.Application
import androidx.room.Room
import com.lalilu.lhistory.repository.HistoryDao
import com.lalilu.lhistory.repository.LDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.lalilu.lhistory")
object HistoryModule

@Single
fun provideRoom(
    application: Application
): LDatabase {
    return Room.databaseBuilder(application, LDatabase::class.java, "lmedia.db")
        .fallbackToDestructiveMigration()
        .build()
}

@Single
fun provideHistoryDao(database: LDatabase): HistoryDao {
    return database.historyDao()
}