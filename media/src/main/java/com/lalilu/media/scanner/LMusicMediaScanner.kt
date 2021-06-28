package com.lalilu.media.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.lalilu.common.bitmap.BitmapUtils
import com.lalilu.media.database.LMusicDatabase
import com.lalilu.media.entity.Music
import java.io.File

/**
 *  针对LMusicMedia对象的BaseMediaScanner实现，
 */
class LMusicMediaScanner(
    private val mContext: Context,
    database: LMusicDatabase
) : BaseMediaScanner<Music>() {
    private val musicDao = database.musicDao()
    private var onScanCallback: MediaScanner.OnScanCallback<Music>? = null
    private var standardDirectory: File =
        mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    init {
        selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                "and ${MediaStore.Audio.Media.DURATION} >= ?" +
                "and ${MediaStore.Audio.Artists.ARTIST} != ?"
        selectionArgs =
            arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")
    }

    fun setOnScanCallback(callback: MediaScanner.OnScanCallback<Music>)
            : LMusicMediaScanner {
        this.onScanCallback = callback
        return this
    }

    override fun onScanProgress(nowCount: Int, item: Music) {
        onScanCallback?.onScanProgress(nowCount, item)
    }

    /**
     * 清空歌曲数据库并回调onScanStart
     */
    override fun onScanStart(totalCount: Int) {
        musicDao.deleteAll()
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
    override fun onScanForEach(cursor: Cursor): Music {
        return getMusic(cursor).also {
            BitmapUtils.saveThumbnailToSandBox(
                mContext, standardDirectory,
                it.musicId, it.musicUri
            )
            it.musicArtUri =
                BitmapUtils.loadThumbnail(standardDirectory, it.musicId)
            musicDao.insert(it)
        }
    }

    companion object {
        private val externalAlbumUri: Uri = Uri.parse("content://media/external/audio/albumart")

        private fun getMusic(cursor: Cursor): Music {
            val mediaId = getLong(cursor, MediaStore.Audio.Media._ID)
            val mediaSize = getLong(cursor, MediaStore.Audio.Media.SIZE)
            val mediaArtist = getString(cursor, MediaStore.Audio.Artists.ARTIST)
            val mediaDuration = getLong(cursor, MediaStore.Audio.Media.DURATION)
            val mediaTitle = getString(cursor, MediaStore.Audio.Media.TITLE)
            val mediaMimeType = getString(cursor, MediaStore.Audio.Media.MIME_TYPE)
            val mediaUri = Uri.withAppendedPath(EXTERNAL_CONTENT_URI, mediaId.toString())
            val albumTitle = getString(cursor, MediaStore.Audio.Albums.ALBUM)
            return Music(
                mediaId,
                mediaTitle,
                mediaSize,
                mediaDuration,
                mediaArtist,
                mediaMimeType,
                mediaUri,
                albumTitle
            )
        }

        private fun getString(cursor: Cursor, string: String): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(string))
        }

        private fun getLong(cursor: Cursor, string: String): Long {
            return cursor.getLong(cursor.getColumnIndexOrThrow(string))
        }
    }
}