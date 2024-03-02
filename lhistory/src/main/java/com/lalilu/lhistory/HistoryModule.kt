package com.lalilu.lhistory

import androidx.room.Room
import com.lalilu.lhistory.repository.HistoryRepository
import com.lalilu.lhistory.repository.HistoryRepositoryImpl
import com.lalilu.lhistory.repository.LDatabase
import com.lalilu.lhistory.screen.HistoryScreenModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val HistoryModule = module {
    single {
        Room.databaseBuilder(androidApplication(), LDatabase::class.java, "lmedia.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single<HistoryRepository> { HistoryRepositoryImpl(get<LDatabase>().historyDao()) }
    factoryOf(::HistoryScreenModel)
}