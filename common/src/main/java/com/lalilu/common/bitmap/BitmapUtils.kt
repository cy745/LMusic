package com.lalilu.common.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream

class BitmapUtils {
    companion object {
        fun saveThumbnailToSandBox(
            context: Context,
            path: File,
            mediaItemId: Long,
            mediaItemUri: Uri
        ) {
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(context, mediaItemUri)

            val embeddedPic = metadataRetriever.embeddedPicture ?: return
            val outputStream = FileOutputStream("$path/${mediaItemId}")
            outputStream.write(embeddedPic)
            outputStream.flush()
            outputStream.close()
        }

        fun loadThumbnail(path: File, mediaItemId: Long): Uri {
            val file = File("$path/${mediaItemId}")
            return Uri.fromFile(file)
        }

        fun loadBitmapFromUri(uri: Uri, toSize: Int): Bitmap? {
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
}