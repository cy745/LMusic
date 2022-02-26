package com.lalilu.lmusic.datasource

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.lalilu.lmusic.domain.entity.MAlbum
import com.lalilu.lmusic.domain.entity.MArtist
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class BaseMediaSource @Inject constructor(
    @ApplicationContext val mContext: Context,
) : CoroutineScope, AbstractMediaSource() {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val targetUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private var data: MutableList<MediaMetadataCompat> = ArrayList()
    override fun iterator(): Iterator<MediaMetadataCompat> = data.iterator()

    inner class MediaSourceObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            launch(Dispatchers.IO) {
                _songs.emit(getAllSongs())
                _albums.emit(getAllAlbums())
                _artists.emit(getAllArtists())
            }
        }
    }

    override suspend fun load() {
        try {
            readyState = STATE_INITIALIZING
            data = getMediaItems()
            readyState = STATE_INITIALIZED
        } catch (e: Exception) {
            readyState = STATE_ERROR
        }
    }

    fun getMediaItems(): MutableList<MediaMetadataCompat> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE
            ),
            selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                    "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                    "and ${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf("$minSizeLimit", "$minDurationLimit", unknownArtist)
        ) ?: return ArrayList()

        return ArrayList<MediaMetadataCompat>().apply {
            while (cursor.moveToNext()) {
                add(
                    MediaMetadataCompat.Builder()
                        .from(cursor)
                        .build()
                )
            }
        }
    }

    init {
        mContext.contentResolver
            .registerContentObserver(targetUri, true, MediaSourceObserver())
    }

    val minDurationLimit = 30 * 1000
    val minSizeLimit = 500 * 1024
    val unknownArtist = "<unknown>"

    val _songs: MutableStateFlow<List<MSong>> = MutableStateFlow(ArrayList())
    val _albums: MutableStateFlow<List<MAlbum>> = MutableStateFlow(ArrayList())
    val _artists: MutableStateFlow<List<MArtist>> = MutableStateFlow(ArrayList())

    val songs: LiveData<List<MSong>> = _songs.asLiveData()
    val albums: LiveData<List<MAlbum>> = _albums.asLiveData()
    val artists: LiveData<List<MArtist>> = _artists.asLiveData()

    private fun searchForMedia(
        selection: String? = null,
        projection: Array<String>? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Cursor? {
        return mContext.contentResolver.query(
            targetUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    override fun getAllSongs(): List<MSong> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE
            ),
            selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                    "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                    "and ${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf("$minSizeLimit", "$minDurationLimit", unknownArtist)
        ) ?: return emptyList()
        return ArrayList<MSong>().apply {
            while (cursor.moveToNext()) {
                add(
                    MSong(
                        songId = cursor.getSongId(),                    // 音乐 id
                        albumId = cursor.getAlbumId(),                  // 专辑 id
                        songData = cursor.getSongData(),                // 路径
                        songTitle = cursor.getSongTitle(),              // 歌曲标题
                        albumTitle = cursor.getAlbumTitle(),            // 专辑标题
                        showingArtist = cursor.getArtist(),             // 艺术家
                        songDuration = cursor.getSongDuration(),        // 音乐时长
                        songMimeType = cursor.getSongMimeType(),        // MIME类型
                    )
                )
            }
        }
    }

    override fun getAllAlbums(): List<MAlbum> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST
            ),
            selection = "${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf(unknownArtist)
        ) ?: return emptyList()
        return LinkedHashSet<MAlbum>().apply {
            while (cursor.moveToNext()) {
                val albumId = cursor.getAlbumId()                       // 专辑 id
                val albumTitle = cursor.getAlbumTitle()                 // 专辑标题
                val artistId = cursor.getArtistId()                     // 艺术家 id
                val artistName = cursor.getArtist()                     // 艺术家

                add(
                    MAlbum(
                        albumId = albumId,
                        albumTitle = albumTitle,
                        artistId = artistId,
                        artistName = artistName
                    )
                )
            }
        }.toList()
    }

    override fun getAllArtists(): List<MArtist> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ARTIST
            ),
            selection = "${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf(unknownArtist)
        ) ?: return emptyList()

        return LinkedHashSet<MArtist>().apply {
            while (cursor.moveToNext()) {
                val artistId = cursor.getArtistId()                     // 艺术家 id
                val artistName = cursor.getArtist()                     // 艺术家

                add(
                    MArtist(
                        artistId = artistId,
                        artistName = artistName
                    )
                )
            }
        }.toList()
    }

    override fun getSongsByAlbumId(id: Long): List<MSong> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE
            ),
            selection = "${MediaStore.Audio.Media.ALBUM_ID} == ? " +
                    "and ${MediaStore.Audio.Media.SIZE} >= ? " +
                    "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                    "and ${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf(
                "$id",
                "$minSizeLimit",
                "$minDurationLimit",
                unknownArtist,
            )
        ) ?: return emptyList()
        return ArrayList<MSong>().apply {
            while (cursor.moveToNext()) {
                add(
                    MSong(
                        songId = cursor.getSongId(),                    // 音乐 id
                        albumId = cursor.getAlbumId(),                  // 专辑 id
                        songData = cursor.getSongData(),                // 路径
                        songTitle = cursor.getSongTitle(),              // 歌曲标题
                        albumTitle = cursor.getAlbumTitle(),            // 专辑标题
                        showingArtist = cursor.getArtist(),             // 艺术家
                        songDuration = cursor.getSongDuration(),        // 音乐时长
                        songMimeType = cursor.getSongMimeType(),        // MIME类型
                    )
                )
            }
        }
    }

    override fun getSongsByArtistId(id: Long): List<MSong> {
        val cursor = searchForMedia(
            projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE
            ),
            selection = "${MediaStore.Audio.Media.ARTIST_ID} == ? " +
                    "and ${MediaStore.Audio.Media.SIZE} >= ? " +
                    "and ${MediaStore.Audio.Media.DURATION} >= ? " +
                    "and ${MediaStore.Audio.Artists.ARTIST} != ?",
            selectionArgs = arrayOf(
                "$id",
                "$minSizeLimit",
                "$minDurationLimit",
                unknownArtist,
            )
        ) ?: return emptyList()
        return ArrayList<MSong>().apply {
            while (cursor.moveToNext()) {
                val songSize = cursor.getSongSize()                     // 大小
                val artistsName = cursor.getArtists().toTypedArray()    // 艺术家

                add(
                    MSong(
                        songId = cursor.getSongId(),                    // 音乐 id
                        albumId = cursor.getAlbumId(),                  // 专辑 id
                        songData = cursor.getSongData(),                // 路径
                        songTitle = cursor.getSongTitle(),              // 歌曲标题
                        albumTitle = cursor.getAlbumTitle(),            // 专辑标题
                        showingArtist = cursor.getArtist(),             // 艺术家
                        songDuration = cursor.getSongDuration(),        // 音乐时长
                        songMimeType = cursor.getSongMimeType(),        // MIME类型
                    )
                )
            }
        }
    }

    override fun getSongById(id: Long): MSong {
        return getAllSongs().first { it.songId == id }
    }

    override fun getAlbumById(id: Long): MAlbum {
        return getAllAlbums().first { it.albumId == id }
    }

    override fun getArtistById(id: Long): MArtist {
        return getAllArtists().first { it.artistId == id }
    }

    override fun getSongsBySongIds(ids: List<Long>): List<MSong> {
        return getAllSongs().filter { ids.contains(it.songId) }
    }
}