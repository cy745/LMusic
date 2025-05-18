package com.lalilu.lmedia.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.database.getIntOrNull
import com.lalilu.lmedia.extension.unpackDiscNo
import com.lalilu.lmedia.extension.unpackTrackNo
import java.io.File

@SuppressLint("ObsoleteSdkInt", "SupportAnnotationUsage")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class Api21MediaStoreScanner(context: Context) : MediaStoreScanner(context) {
    private var trackIndex = -1
    private var dataIndex = -1

    override val projection: Array<String> = super.projection + arrayOf(
        MediaStore.Audio.AudioColumns.TRACK,
        MediaStore.Audio.AudioColumns.DATA
    )

    @SuppressLint("Range")
    override fun buildAudio(cursor: Cursor): Audio {
        val audio = super.buildAudio(cursor)
        if (trackIndex == -1) {
            trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
            dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
        }

        val data = cursor.getString(dataIndex)
        audio.data = data

        if (audio.fileName == null && data != null) {
            audio.fileName = data.substringAfterLast(File.separatorChar, "")
                .ifEmpty { null }
        }

        val rawTrack = cursor.getIntOrNull(trackIndex)
        if (rawTrack != null) {
            rawTrack.unpackTrackNo()?.let { audio.track = it }
            rawTrack.unpackDiscNo()?.let { audio.disc = it }
        }

        return audio
    }
}