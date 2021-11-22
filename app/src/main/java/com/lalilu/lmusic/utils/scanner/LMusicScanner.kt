package com.lalilu.lmusic.utils.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.lalilu.lmusic.domain.entity.LSong
import com.lalilu.lmusic.utils.BitmapUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger


class LMusicScanner(private val mContext: Context) : BaseMediaScanner<LSong>() {
    var onScanCallback: MediaScanner.OnScanCallback<LSong>? = null

    private var standardDirectory: File =
        mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

    init {
        selection = "${MediaStore.Audio.Media.SIZE} >= ? " +
                "and ${MediaStore.Audio.Media.DURATION} >= ?" +
                "and ${MediaStore.Audio.Artists.ARTIST} != ?"
        selectionArgs =
            arrayOf((500 * 1024).toString(), (30 * 1000).toString(), "<unknown>")
    }

    override fun onScanStart(totalCount: Int) {
        onScanCallback?.onScanStart(totalCount)
    }

    fun setOnScanCallback(callback: MediaScanner.OnScanCallback<LSong>)
            : LMusicScanner {
        this.onScanCallback = callback
        return this
    }

    override fun onScanFinish(resultCount: Int) {
        onScanCallback?.onScanFinish(resultCount)
    }

    override fun onScanFailed(msg: String?) {
        onScanCallback?.onScanException(msg)
    }

    override fun onScanProgress(nowCount: Int, item: LSong) {
        onScanCallback?.onScanProgress(nowCount, item)
    }

    @Throws
    override fun onScanForEach(cursor: Cursor): LSong {
        Logger.getLogger("org.jaudiotagger").level = Level.OFF

        val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE) // 标题，音乐名称
        val songIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID) // 音乐 id
        val mediaDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) // 音乐时长
        val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) // 艺术家
        val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) // 专辑 id
        val albumTitleColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
        val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE) // 大小
        val mimeTypeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE) // mime类型
        val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA) // 路径

        val id = cursor.getLong(songIdColumn)
        val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())


        return LSong(
            mId = id,
            mTitle = cursor.getString(titleColumn),
            mType = LSong.SONG_TYPE_LOCAL,
            mAlbum = LSong.AlbumInfo(
                albumId = cursor.getLong(albumIdColumn),
                albumTitle = cursor.getString(albumTitleColumn)
            ),
            mLocalInfo = LSong.LocalInfo(
                mUri = uri,
                mData = cursor.getString(dataColumn),
                mSize = cursor.getLong(sizeColumn),
                mDuration = cursor.getLong(mediaDuration),
                mMimeType = cursor.getString(mimeTypeColumn)
            ),
            mArtist = cursor.getString(artistColumn).split("/").map {
                LSong.Artist(it)
            }
        ).also {
            it.mArtUri = BitmapUtils.saveThumbnailToSandBox(
                mContext, standardDirectory, it.mId, uri
            )
            try {
                // TODO: 2021/9/6 仍有部分歌曲无法获取到内嵌歌词,原因查明： LyricViewX 无法显示 USLT 歌词
                val audioTag = AudioFileIO.read(File(it.mLocalInfo!!.mData)).tag
                val lyric = audioTag.getFields(FieldKey.LYRICS)
                if (lyric.isNotEmpty()) it.mLocalInfo?.mLyric = lyric[0].toString()
            } catch (e: Exception) {
                println("audioFile:" + e.message)
            }
        }
    }
}