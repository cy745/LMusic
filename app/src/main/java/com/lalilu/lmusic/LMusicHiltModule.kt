package com.lalilu.lmusic

//
//@Module
//@ExperimentalCoroutinesApi
//@InstallIn(SingletonComponent::class)
//object LMusicHiltModule {
//
//
//    @Provides
//    @Singleton
//    fun providesOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
//            .hostnameVerifier { _, _ -> true }
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaDatabase(@ApplicationContext context: Context): LDatabase {
//        return Room.databaseBuilder(context, LDatabase::class.java, "lmedia_database.db")
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaPlaylistRepoImpl(database: LDatabase): PlaylistRepositoryImpl {
//        return PlaylistRepositoryImpl(
//            playlistDao = database.playlistDao(),
//            songInPlaylistDao = database.songInPlaylistDao(),
//            getSongOrNull = LMedia::getSongOrNull
//        )
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaPlaylistRepo(impl: PlaylistRepositoryImpl): PlaylistRepository {
//        return impl
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaFavoriteRepo(impl: PlaylistRepositoryImpl): FavoriteRepository {
//        return impl
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaHistoryRepo(database: LDatabase): HistoryRepository {
//        return HistoryRepositoryImpl(
//            historyDao = database.historyDao()
//        )
//    }
//
//    @Provides
//    @Singleton
//    fun provideLMediaNetDataRepo(database: LDatabase): NetDataRepository {
//        return NetDataRepositoryImpl(
//            netDataDao = database.netDataDao()
//        )
//    }
//}