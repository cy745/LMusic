package com.lalilu.lmusic.utils.filter

import androidx.compose.ui.util.fastAny
import com.blankj.utilcode.util.FileUtils
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LDictionary
import com.lalilu.lmedia.entity.LGenre
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.IndexFilter
import com.lalilu.lmusic.datastore.LMusicSp

class DictionaryFilter(
    private val lmusicSp: LMusicSp
) : IndexFilter {
    override fun onSongsBuilt(songs: List<LSong>): List<LSong> {
        val blockedPaths = lmusicSp.blockedPaths.get() ?: emptyList()

        return songs.onEach {
            val path = FileUtils.getDirName(it.pathStr)
                ?.takeIf(String::isNotEmpty)
                ?: "Unknown dir"
            it.blocked = blockedPaths.contains(path)
        }
    }

    override fun onAlbumsBuilt(albums: List<LAlbum>): List<LAlbum> {
        return albums.onEach {
            it.blocked = !it.songs.fastAny { songs -> !songs.blocked }
        }
    }

    override fun onArtistsBuilt(artists: List<LArtist>): List<LArtist> {
        return artists.onEach {
            it.blocked = !it.songs.fastAny { songs -> !songs.blocked }
        }
    }

    override fun onGenresBuilt(genres: List<LGenre>): List<LGenre> {
        return genres.onEach {
            it.blocked = !it.songs.fastAny { songs -> !songs.blocked }
        }
    }

    override fun onDictionariesBuilt(dictionaries: List<LDictionary>): List<LDictionary> {
        val blockedPaths = lmusicSp.blockedPaths.get() ?: emptyList()

        return dictionaries.onEach {
            it.blocked = blockedPaths.contains(it.path)
        }
    }
}