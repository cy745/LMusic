package com.lalilu.lmusic.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lalilu.lmusic.datasource.converter.DateConverter
import com.lalilu.lmusic.datasource.converter.UriConverter
import com.lalilu.lmusic.datasource.dao.MLyricDao
import com.lalilu.lmusic.datasource.dao.MPlaylistDao
import com.lalilu.lmusic.datasource.dao.SongInPlaylistDao
import com.lalilu.lmusic.datasource.entity.*

@Database(
    entities = [
        MPlaylist::class,
        MLyric::class,
        MArtist::class,
        SongInPlaylist::class,
        CustomArtistMapToOriginArtist::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(
    UriConverter::class,
    DateConverter::class
)
abstract class MDataBase : RoomDatabase() {
    abstract fun lyricDao(): MLyricDao
    abstract fun playlistDao(): MPlaylistDao
    abstract fun songInPlaylistDao(): SongInPlaylistDao
}
