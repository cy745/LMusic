package com.lalilu.lmedia

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.MediaItem
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LFolder
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.toMediaItem
import com.lalilu.lmedia.indexer.BaseLibrary
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmedia.repository.LMediaSp
import com.lalilu.lmedia.scanner.FileSystemScanner
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object LMedia : BaseLibrary(), Library {
    const val ARTIST_PREFIX = "artist_"
    const val ALBUM_PREFIX = "album_"
    const val GENRE_PREFIX = "genre_"
    const val FOLDER_PREFIX = "folder_"

    const val ROOT = "root"
    const val ALL_SONGS = "all_songs"
    const val ALL_ARTISTS = "all_artists"
    const val ALL_ALBUMS = "all_albums"

    private var indexer: Indexer? = null

    fun init(context: Context) {
        indexer = (indexer ?: Indexer(this))
            .also { it.init(context) }
    }

    override fun getItem(mediaId: String): MediaItem? {
        return when {
            mediaId.startsWith(ARTIST_PREFIX) -> get<LArtist>(mediaId)?.toMediaItem()
            mediaId.startsWith(ALBUM_PREFIX) -> get<LAlbum>(mediaId)?.toMediaItem()
            mediaId.startsWith(GENRE_PREFIX) -> get<LGenre>(mediaId)?.toMediaItem()
            mediaId.startsWith(FOLDER_PREFIX) -> get<LFolder>(mediaId)?.toMediaItem()
            else -> get<LSong>(mediaId)?.toMediaItem()
        }
    }

    override fun mapItems(mediaIds: List<String>): List<MediaItem> {
        return mediaIds.mapNotNull { getItem(mediaId = it) }
    }

    override fun getChildren(parentId: String): List<MediaItem> {
        return when {
            parentId == "all_songs" -> {
                get<LSong>().map { it.toMediaItem() }
            }

            parentId.startsWith(ARTIST_PREFIX) -> {
                val mediaId = parentId.substring(ARTIST_PREFIX.length)
                get<LArtist>(mediaId)?.songs?.map { it.toMediaItem() }
            }

            parentId.startsWith(ALBUM_PREFIX) -> {
                val mediaId = parentId.substring(ALBUM_PREFIX.length)
                get<LAlbum>(mediaId)?.songs?.map { it.toMediaItem() }
            }

            parentId.startsWith(GENRE_PREFIX) -> {
                val mediaId = parentId.substring(GENRE_PREFIX.length)
                get<LGenre>(mediaId)?.songs?.map { it.toMediaItem() }
            }

            parentId.startsWith(FOLDER_PREFIX) -> {
                val mediaId = parentId.substring(FOLDER_PREFIX.length)
                get<LFolder>(mediaId)?.songs?.map { it.toMediaItem() }
            }

            else -> emptyList()
        } ?: emptyList()
    }

    @SuppressLint("ObsoleteSdkInt")
    val module = module {
        singleOf(::LMediaSp)
        singleOf(::FileSystemScanner)
    }
}