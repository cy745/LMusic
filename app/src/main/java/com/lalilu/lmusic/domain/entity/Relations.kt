package com.lalilu.lmusic.domain.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*

/**
 * 由于专辑的 id 是 MediaStore 自动分发的，
 * 所以并不存在多对多关系，只有一对多关系
 * 获取某专辑下的所有歌曲
 */
data class AlbumWithSongs(
    @Embedded val album: MAlbum,
    @Relation(
        parentColumn = "album_id",
        entityColumn = "album_id",
    )
    val songs: List<MSong>
)

/**
 * **************************************
 *         Artist 和 Song 联查
 * **************************************
 *
 * 获取某一位歌手的所有歌曲
 */
data class ArtistWithSongs(
    @Embedded val artist: MArtist,
    @Relation(
        parentColumn = "artist_name",
        entityColumn = "song_id",
        associateBy = Junction(ArtistSongCrossRef::class)
    )
    val songs: List<MSong>
)


/**
 * 获取某一首歌曲的所有相关信息
 */
data class FullSongInfo(
    @Embedded val song: MSong,

    @Relation(
        parentColumn = "song_id",
        entityColumn = "artist_name",
        associateBy = Junction(ArtistSongCrossRef::class)
    )
    val artists: List<MArtist> = ArrayList(),

    @Relation(
        parentColumn = "album_id",
        entityColumn = "album_id",
    )
    val album: MAlbum? = null,

    @Relation(
        parentColumn = "song_id",
        entityColumn = "song_id",
    )
    val detail: MSongDetail? = null
)


/**
 * MSong 和 MArtist 之间是多对多的关系
 */
@Entity(
    tableName = "artist_song_cross_ref",
    primaryKeys = ["song_id", "artist_name"],
    foreignKeys = [ForeignKey(
        entity = MSong::class,
        parentColumns = ["song_id"],
        childColumns = ["song_id"],
        onDelete = CASCADE
    ), ForeignKey(
        entity = MArtist::class,
        parentColumns = ["artist_name"],
        childColumns = ["artist_name"],
        onDelete = CASCADE
    )]
)
data class ArtistSongCrossRef(
    @ColumnInfo(name = "song_id", index = true)
    val songId: Long,
    @ColumnInfo(name = "artist_name", index = true)
    val artistName: String
)


/**
 * **************************************
 *         Playlist 和 Song 联查
 * **************************************
 *
 * 获取包含某一首歌单下的所有歌曲
 */
data class PlaylistWithSongs(
    @Embedded val playlist: MPlaylist,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "song_id",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    var songs: List<MSong>
)


/**
 * 获取包含某一首歌的所有歌单
 */
data class SongWithPlaylists(
    @Embedded val song: MSong,
    @Relation(
        parentColumn = "song_id",
        entityColumn = "playlist_id",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val playlists: List<MPlaylist>
)


/**
 * MSong 和 MPlaylist 之间是多对多的关系
 */
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["song_id", "playlist_id"],
    foreignKeys = [ForeignKey(
        entity = MSong::class,
        parentColumns = ["song_id"],
        childColumns = ["song_id"],
        onDelete = CASCADE
    ), ForeignKey(
        entity = MPlaylist::class,
        parentColumns = ["playlist_id"],
        childColumns = ["playlist_id"],
        onDelete = CASCADE
    )]
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "song_id", index = true)
    val songId: Long,
    @ColumnInfo(name = "playlist_id", index = true)
    val playlistId: Long,
    @ColumnInfo(name = "create_item", index = true)
    val createItem: Date = Date(),
)