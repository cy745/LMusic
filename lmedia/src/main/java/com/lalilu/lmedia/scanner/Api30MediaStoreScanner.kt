package com.lalilu.lmedia.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.lalilu.lmedia.extension.parsePositionNum

@RequiresApi(Build.VERSION_CODES.R)
class Api30MediaStoreScanner(context: Context) : Api21MediaStoreScanner(context) {
    private var trackIndex = -1
    private var discIndex = -1
    private var bitrateIndex = -1

    override val projection: Array<String> = super.projection + arrayOf(
        MediaStore.Audio.AudioColumns.CD_TRACK_NUMBER,
        MediaStore.Audio.AudioColumns.DISC_NUMBER,
        MediaStore.Audio.AudioColumns.BITRATE,
    )

    @SuppressLint("Range")
    override fun buildAudio(cursor: Cursor): Audio {
        val audio = super.buildAudio(cursor)

        // Populate our indices if we have not already.
        if (trackIndex == -1) {
            trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.CD_TRACK_NUMBER)
            discIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISC_NUMBER)
            bitrateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BITRATE)
        }

        // Both CD_TRACK_NUMBER and DISC_NUMBER tend to be formatted as they are in
        // the tag itself, which is to say that it is formatted as NN/TT tracks, where
        // N is the number and T is the total. Parse the number while leaving out the
        // total, as we have no use for it.
        cursor.getStringOrNull(trackIndex)?.parsePositionNum()?.let { audio.track = it }
        cursor.getStringOrNull(discIndex)?.parsePositionNum()?.let { audio.disc = it }
        cursor.getIntOrNull(bitrateIndex)?.let { audio.bitrate = it }

        return audio
    }
}
