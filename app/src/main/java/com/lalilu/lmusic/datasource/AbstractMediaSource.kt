package com.lalilu.lmusic.datasource

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import com.lalilu.lmusic.datasource.extensions.containsCaseInsensitive
import com.lalilu.lmusic.utils.BaseReadyHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class AbstractMediaSource : BaseReadyHelper(), MediaSource, CoroutineScope {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    abstract fun getAlbumIdFromMediaItem(mediaItem: MediaItem): String
    abstract fun getArtistIdFromMediaItem(mediaItem: MediaItem): String
    abstract fun songItemToAlbumItem(mediaItem: MediaItem): MediaItem
    abstract fun songItemToArtistItem(mediaItem: MediaItem): MediaItem
    abstract fun songItemToGenreItem(mediaItem: MediaItem): MediaItem

    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    fun getItemById(id: String): MediaItem? {
        return treeNodes[id]?.item
    }

    fun getChildren(parentId: String): List<MediaItem>? {
        return treeNodes[parentId]?.getChildren()
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

    override fun search(query: String, extras: Bundle): List<MediaItem> {
        val focusSearchResult = when (extras[MediaStore.EXTRA_MEDIA_FOCUS]) {
            MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE -> {
                // For a Genre focused search, only genre is set.
                val genre = extras[MediaStore.EXTRA_MEDIA_GENRE]
                treeNodes[GENRE_ID]?.getChildren()?.filter {
                    it.mediaMetadata.genre.toString() == genre
                            && it.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                } ?: emptyList()
            }
            MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> {
                // For an Artist focused search, only the artist is set.
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                treeNodes[ARTIST_ID]?.getChildren()?.filter {
                    (it.mediaMetadata.artist.toString() == artist ||
                            it.mediaMetadata.albumArtist.toString() == artist)
                            && it.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                } ?: emptyList()
            }
            MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE -> {
                // For an Album focused search, album and artist are set.
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                val albumTitle = extras[MediaStore.EXTRA_MEDIA_ALBUM]
                treeNodes[ALBUM_ID]?.getChildren()?.filter {
                    (it.mediaMetadata.artist.toString() == artist ||
                            it.mediaMetadata.albumArtist.toString() == artist)
                            && it.mediaMetadata.albumTitle.toString() == albumTitle
                            && it.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                } ?: emptyList()
            }
            MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                // For a Song (aka Media) focused search, title, album, and artist are set.
                val title = extras[MediaStore.EXTRA_MEDIA_TITLE]
                val albumTitle = extras[MediaStore.EXTRA_MEDIA_ALBUM]
                val artist = extras[MediaStore.EXTRA_MEDIA_ARTIST]
                treeNodes.values.filter {
                    (it.item.mediaMetadata.artist.toString() == artist ||
                            it.item.mediaMetadata.albumArtist.toString() == artist)
                            && it.item.mediaMetadata.albumTitle.toString() == albumTitle
                            && it.item.mediaMetadata.title.toString() == title
                            && it.item.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                }.map { it.item }
            }
            else -> {
                emptyList()
            }
        }
        if (focusSearchResult.isEmpty()) {
            return if (query.isNotBlank()) {
                treeNodes.values.filter {
                    (it.item.mediaMetadata.title.toString().containsCaseInsensitive(query)
                            || it.item.mediaMetadata.genre.toString()
                        .containsCaseInsensitive(query))
                            && it.item.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                }.map { it.item }
            } else {
                // If the user asked to "play music", or something similar, the query will also
                // be blank. Given the small catalog of songs in the sample, just return them
                // all, shuffled, as something to play.
                return treeNodes.values.filter {
                    it.item.mediaMetadata.folderType == MediaMetadata.FOLDER_TYPE_NONE
                }.map { it.item }.shuffled()
            }
        } else {
            return focusSearchResult
        }
    }
}
