package com.lalilu.media.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.lalilu.common.bitmap.BitmapUtils
import com.lalilu.media.database.LMusicDatabase
import com.lalilu.media.entity.LMusicMedia
import java.io.File

/**
 *  针对LMusicMedia对象的BaseMediaScanner实现，
 */
class LMusicMediaScanner(
    private val mContext: Context,
    database: LMusicDatabase
) : BaseMediaScanner<LMusicMedia>() {
    private val mediaItemDao = database.mediaItemDao()
    private var onScanCallback: MediaScanner.OnScanCallback<LMusicMedia>? = null
    private var standardDirectory: File =
        mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    init {
        selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                "and ${MediaStore.Audio.Media.DURATION} >= ?" +
                "and ${MediaStore.Audio.Artists.ARTIST} != ?"
        selectionArgs =
            arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")
    }

    fun setOnScanCallback(callback: MediaScanner.OnScanCallback<LMusicMedia>)
            : LMusicMediaScanner {
        this.onScanCallback = callback
        return this
    }

    override fun onScanProgress(nowCount: Int, item: LMusicMedia) {
        onScanCallback?.onScanProgress(nowCount, item)
    }

    /**
     * 清空歌曲数据库并回调onScanStart
     */
    override fun onScanStart(totalCount: Int) {
        mediaItemDao.deleteAll()
        onScanCallback?.onScanStart(totalCount)
    }

    override fun onScanFinish(resultCount: Int) {
        onScanCallback?.onScanFinish(resultCount)
    }

    /**
     *  从cursor中提取LMusicMedia，
     *  提取音频文件中嵌入的封面并存到应用的私有空间中，
     *  最后将LMusicMedia存入数据库
     */
    override fun onScanForEach(cursor: Cursor): LMusicMedia {
        return getLMusicMedia(cursor).also {
            BitmapUtils.saveThumbnailToSandBox(
                mContext, standardDirectory,
                it.mediaId, it.mediaUri
            )
            it.mediaArtUri =
                BitmapUtils.loadThumbnail(standardDirectory, it.mediaId)
            mediaItemDao.insert(it)
        }
    }

    companion object {
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")

        private fun getLMusicMedia(cursor: Cursor): LMusicMedia {
            return LMusicMedia().also {
                it.mediaId = getLong(cursor, MediaStore.Audio.Media._ID)
                it.mediaSize = getLong(cursor, MediaStore.Audio.Media.SIZE)
                it.mediaArtist = getString(cursor, MediaStore.Audio.Artists.ARTIST)
                it.mediaDuration = getLong(cursor, MediaStore.Audio.Media.DURATION)
                it.mediaTitle = getString(cursor, MediaStore.Audio.Media.TITLE)
                it.mediaMimeType = getString(cursor, MediaStore.Audio.Media.MIME_TYPE)
                it.albumId = getLong(cursor, MediaStore.Audio.Albums.ALBUM_ID)
                it.albumTitle = getString(cursor, MediaStore.Audio.Albums.ALBUM)
                it.albumArtist = getString(cursor, MediaStore.Audio.Albums.ARTIST)
                it.albumUri = ContentUris.withAppendedId(externalAlbumUri, it.albumId)
                it.mediaUri = Uri.withAppendedPath(EXTERNAL_CONTENT_URI, it.mediaId.toString())
            }
        }

        private fun getString(cursor: Cursor, string: String): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(string))
        }

        private fun getLong(cursor: Cursor, string: String): Long {
            return cursor.getLong(cursor.getColumnIndexOrThrow(string))
        }
    }
}