package com.lalilu.lmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {
    fun saveThumbnailToSandBox(
        context: Context,
        mediaItemId: Long,
        mediaItemUri: Uri
    ): Uri {
        val path: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        try {
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(context, mediaItemUri)

            val embeddedPic = metadataRetriever.embeddedPicture ?: return Uri.EMPTY
            val outputStream = FileOutputStream("$path/${mediaItemId}")
            outputStream.write(embeddedPic)
            outputStream.flush()
            outputStream.close()
            metadataRetriever.release()
        } catch (e: Exception) {
            return Uri.EMPTY
        }
        return loadThumbnail(path, mediaItemId)
    }

    fun loadThumbnail(path: File, mediaItemId: Long): Uri {
        val file = File("$path/${mediaItemId}")
        return Uri.fromFile(file)
    }

    fun loadBitmapFromUri(uri: Uri?, toSize: Int): Bitmap? {
        uri ?: return null
        val outOption = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
        }
        try {
            BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)
        } catch (e: Exception) {
            return null
        }

        val outWidth = outOption.outWidth
        val outHeight = outOption.outHeight
        if (outWidth == -1 || outHeight == -1) return null

        var scaleValue: Int = if (outWidth > toSize) outWidth / toSize else toSize / outWidth
        if (scaleValue < 1) scaleValue = 1

        outOption.also {
            it.inJustDecodeBounds = false
            it.inSampleSize = scaleValue
        }

        return BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)
    }
}