package com.lalilu.lmusic.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.datasource.converter.DateConverter
import com.lalilu.lmusic.datasource.converter.UriConverter
import com.lalilu.lmusic.datasource.dao.MArtistDao
import com.lalilu.lmusic.datasource.dao.MLyricDao
import com.lalilu.lmusic.datasource.dao.MPlaylistDao
import com.lalilu.lmusic.datasource.dao.SongInPlaylistDao
import com.lalilu.lmusic.datasource.entity.*

@Database(
    entities = [
        MLyric::class,
        MArtist::class,
        MPlaylist::class,
        SongInPlaylist::class,
        MArtistMapId::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class MDataBase : RoomDatabase() {
    abstract fun lyricDao(): MLyricDao
    abstract fun artistDao(): MArtistDao
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songInPlaylistDao(): SongInPlaylistDao
}
