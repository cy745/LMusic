package com.lalilu.lhistory

import androidx.room.Room
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lhistory.repository.HistoryRepositoryImpl
import com.lalilu.lhistory.repository.LDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val HistoryModule = module {
    single {
        Room.databaseBuilder(androidApplication(), LDatabase::class.java, "lmedia.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single<HistoryRepository> { HistoryRepositoryImpl(get<LDatabase>().historyDao()) }
}