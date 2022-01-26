package com.lalilu.lmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import java.io.File
import java.io.FileOutputStream


fun Drawable.toBitmap(): Bitmap {
    val bd = this as BitmapDrawable
    return bd.bitmap
}

object BitmapUtils {
    fun saveThumbnailToSandBox(
        context: Context,
        mediaItemUri: Uri
    ): Uri {
        val path: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        try {
            val metadataRetriever = MediaMetadataRetriever()
            metadataRetriever.setDataSource(context, mediaItemUri)

            val bytes = metadataRetriever.embeddedPicture ?: return Uri.EMPTY
            val md5 = Md5Utils.getMd5(bytes)

            val file = File("$path/$md5")
            if (file.exists()) return Uri.fromFile(file)

            val outputStream = FileOutputStream(file)
            outputStream.write(bytes)
            outputStream.flush()
            outputStream.close()
            metadataRetriever.close()
            metadataRetriever.release()

            return Uri.fromFile(file)
        } catch (e: Exception) {
            return Uri.EMPTY
        }
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