package com.lalilu.lmedia.scanner

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.lalilu.common.base.SourceType
import com.lalilu.lmedia.entity.FileInfo
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.Metadata
import com.lalilu.lmedia.extension.mediaUri
import java.net.URLDecoder

data class Audio(
    var id: Long,
    var title: String,
    var album: String,
    var albumId: Long,
    var artist: String,
    var artistId: String,
    var albumArtist: String? = null,
    var data: String? = null,
    var dateAdded: Long? = null,
    var dateModified: Long? = null,
    var fileName: String? = null,
    var extensionMimeType: String,
    var formatMimeType: String? = null,
    var duration: Long,
    var size: Long,
    var bitrate: Int = -1,
    var date: String? = null,
    var track: Int? = null,
    var disc: Int? = null
) {
    companion object {
        const val INVALID_ARTIST = "<unknown>"
        const val INVALID_PATH = "<unknown_path>"
    }

    fun toSong(
        genre: String = ""
    ): LSong? {
        try {
            val pathPath = runCatching { URLDecoder.decode(data, "UTF-8") }
                .getOrNull()

            val directoryPath = FileUtils.getDirName(pathPath)
                ?.takeIf(String::isNotEmpty)
                ?: "Unknown dir"

            return LSong(
                // Assert that the fields that should always exist are present. I can't confirm
                // that every device provides these fields, but it seems likely that they do.
                id = id.toString(),
                uri = id.mediaUri(),
                sourceType = SourceType.MediaStore,
                fileInfo = FileInfo(
                    fileName = fileName,
                    mimeType = extensionMimeType,
                    pathStr = pathPath ?: INVALID_PATH,
                    directoryPath = directoryPath,
                    size = size,
                    bitrate = bitrate
                ),
                // TODO 待完善填充MediaStore部分缺失的数据
                metadata = Metadata(
                    title = title,
                    album = album,
                    artist = artist,
                    albumArtist = albumArtist ?: INVALID_ARTIST,
                    composer = "",
                    lyricist = "",
                    comment = "",
                    genre = genre,
                    track = track?.toString() ?: "",
                    disc = disc?.toString() ?: "",
                    date = "",
                    duration = duration,
                    dateAdded = dateAdded ?: 0,
                    dateModified = dateModified ?: 0
                ),
            )
        } catch (e: Exception) {
            LogUtils.e(e.message, e)
            return null
        }
    }
}