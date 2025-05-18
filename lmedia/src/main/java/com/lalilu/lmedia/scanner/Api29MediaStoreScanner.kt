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

@RequiresApi(Build.VERSION_CODES.Q)
class Api29MediaStoreScanner(context: Context) : Api21MediaStoreScanner(context) {
    private var trackIndex = -1

    override val projection: Array<String>
        get() = super.projection + arrayOf(MediaStore.Audio.AudioColumns.TRACK)

    @SuppressLint("Range")
    override fun buildAudio(cursor: Cursor): Audio {
        val audio = super.buildAudio(cursor)

        if (trackIndex == -1) {
            trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK)
        }

        // This backend is volume-aware, but does not support the modern track fields.
        // Use the old field instead.
        val rawTrack = cursor.getIntOrNull(trackIndex)
        if (rawTrack != null) {
            rawTrack.unpackTrackNo()?.let { audio.track = it }
            rawTrack.unpackDiscNo()?.let { audio.disc = it }
        }

        return audio
    }
}
