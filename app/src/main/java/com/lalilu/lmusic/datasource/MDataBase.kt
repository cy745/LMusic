package com.lalilu.lmusic.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.datasource.converter.DateConverter
import com.lalilu.lmusic.datasource.converter.UriConverter
import com.lalilu.lmusic.datasource.dao.MNetworkDataDao
import com.lalilu.lmusic.datasource.dao.MPlaylistDao
import com.lalilu.lmusic.datasource.dao.PlayHistoryDao
import com.lalilu.lmusic.datasource.dao.SongInPlaylistDao
import com.lalilu.lmusic.datasource.entity.MNetworkData
import com.lalilu.lmusic.datasource.entity.MPlaylist
import com.lalilu.lmusic.datasource.entity.PlayHistory
import com.lalilu.lmusic.datasource.entity.SongInPlaylist

@Database(
    entities = [
        MNetworkData::class,
        MPlaylist::class,
        SongInPlaylist::class,
        PlayHistory::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class MDataBase : RoomDatabase() {
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun networkDataDao(): MNetworkDataDao
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songInPlaylistDao(): SongInPlaylistDao
}
