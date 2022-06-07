package com.lalilu.lmusic.datasource

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import com.lalilu.lmusic.datasource.extensions.getAlbumId
import com.lalilu.lmusic.datasource.extensions.getArtistId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

const val ROOT_ID = "[rootID]"
const val ALBUM_ID = "[albumID]"
const val GENRE_ID = "[genreID]"
const val ARTIST_ID = "[artistID]"
const val ALL_ID = "[allID]"

const val ALBUM_PREFIX = "[album]"
const val GENRE_PREFIX = "[genre]"
const val ARTIST_PREFIX = "[artist]"
const val ITEM_PREFIX = "[item]"
const val ALL_PREFIX = "[all]"

interface MediaSource {
    fun getRootItem(): MediaItem
    fun getItemById(key: String): MediaItem?
    fun getChildren(key: String): List<MediaItem>?

    //    fun fillCustomData(item: MediaItem): MediaItem
    fun search(query: String, extras: Bundle): List<MediaItem>
}

abstract class BaseMediaSource : MediaSource, CoroutineScope {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    override fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    override fun getItemById(key: String): MediaItem? {
        return treeNodes[key]?.item
    }

    override fun getChildren(key: String): List<MediaItem>? {
        return treeNodes[key]?.getChildren()
    }

    inner class MediaItemNode(val item: MediaItem) {
        private val children: LinkedHashSet<MediaItem> = LinkedHashSet()

        fun addChild(childID: String) {
            children.add(treeNodes[childID]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    fun buildTree() {
        treeNodes.clear()
        System.gc()
        treeNodes[ROOT_ID] = MediaItemNode(
            buildMediaItem(
                title = "Root Folder",
                mediaId = ROOT_ID,
                isPlayable = false,
                folderType = MediaMetadata.FOLDER_TYPE_MIXED
            )
        )
        treeNodes[ALBUM_ID] = MediaItemNode(
            buildMediaItem(
                title = "Album Folder",
                mediaId = ALBUM_ID,
                isPlayable = false,
                folderType = MediaMetadata.FOLDER_TYPE_MIXED
            )
        )
        treeNodes[ARTIST_ID] = MediaItemNode(
            buildMediaItem(
                title = "Artist Folder",
                mediaId = ARTIST_ID,
                isPlayable = false,
                folderType = MediaMetadata.FOLDER_TYPE_MIXED
            )
        )
        treeNodes[GENRE_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Genre Folder",
                    mediaId = GENRE_ID,
                    isPlayable = false,
                    folderType = MediaMetadata.FOLDER_TYPE_MIXED
                )
            )
        treeNodes[ALL_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "All Items Folder",
                    mediaId = ALL_ID,
                    isPlayable = false,
                    folderType = MediaMetadata.FOLDER_TYPE_PLAYLISTS
                )
            )
        treeNodes[ROOT_ID]!!.addChild(ALBUM_ID)
        treeNodes[ROOT_ID]!!.addChild(ARTIST_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)
        treeNodes[ROOT_ID]!!.addChild(ALL_ID)
    }

    @Throws(Exception::class)
    fun fillWithData(data: List<MediaItem>) {
        buildTree()
        data.forEach {
            val idInTree = ITEM_PREFIX + it.mediaId
            val albumFolderIdInTree = ALBUM_PREFIX + getAlbumIdFromMediaItem(it)
            val artistFolderIdInTree = ARTIST_PREFIX + getArtistIdFromMediaItem(it)
            val genreFolderIdInTree = GENRE_PREFIX + it.mediaMetadata.genre

            treeNodes[idInTree] = MediaItemNode(it)

            if (!treeNodes.containsKey(albumFolderIdInTree)) {
                treeNodes[albumFolderIdInTree] = MediaItemNode(songItemToAlbumItem(it))
                treeNodes[ALBUM_ID]!!.addChild(albumFolderIdInTree)
            }
            if (!treeNodes.containsKey(artistFolderIdInTree)) {
                treeNodes[artistFolderIdInTree] = MediaItemNode(songItemToArtistItem(it))
                treeNodes[ARTIST_ID]!!.addChild(artistFolderIdInTree)
            }
            if (!treeNodes.containsKey(genreFolderIdInTree)) {
                treeNodes[genreFolderIdInTree] = MediaItemNode(songItemToGenreItem(it))
                treeNodes[GENRE_ID]!!.addChild(genreFolderIdInTree)
            }

            treeNodes[albumFolderIdInTree]!!.addChild(idInTree)
            treeNodes[artistFolderIdInTree]!!.addChild(idInTree)
            treeNodes[genreFolderIdInTree]!!.addChild(idInTree)
            treeNodes[ALL_ID]!!.addChild(idInTree)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        @MediaMetadata.FolderType folderType: Int,
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null,
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setAlbumTitle(album)
            .setTitle(title)
            .setArtist(artist)
            .setGenre(genre)
            .setFolderType(folderType)
            .setIsPlayable(isPlayable)
            .setArtworkUri(imageUri)
            .build()
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
    }

    fun getAlbumIdFromMediaItem(mediaItem: MediaItem): String {
        return mediaItem.mediaMetadata.getAlbumId().toString()
    }

    fun getArtistIdFromMediaItem(mediaItem: MediaItem): String {
        return mediaItem.mediaMetadata.getArtistId().toString()
    }

    fun songItemToAlbumItem(mediaItem: MediaItem): MediaItem {
        return mediaItem.buildUpon()
            .setMediaMetadata(
                mediaItem.mediaMetadata.buildUpon()
                    .setIsPlayable(false)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }

    fun songItemToArtistItem(mediaItem: MediaItem): MediaItem {
        return mediaItem.buildUpon()
            .setMediaMetadata(
                mediaItem.mediaMetadata.buildUpon()
                    .setIsPlayable(false)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }

    fun songItemToGenreItem(mediaItem: MediaItem): MediaItem {
        return mediaItem.buildUpon()
            .setMediaId(mediaItem.mediaMetadata.genre.toString())
            .setMediaMetadata(
                mediaItem.mediaMetadata.buildUpon()
                    .setIsPlayable(false)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_PLAYLISTS)
                    .build()
            ).build()
    }
}