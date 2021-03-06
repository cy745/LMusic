package com.lalilu.lmusic.utils.fetcher

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.VisibleForTesting
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.buffer
import okio.source
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentUriFetcher @Inject constructor(
    @ApplicationContext private val context: Context
) : Fetcher<Uri> {

    override fun handles(data: Uri) = data.scheme == ContentResolver.SCHEME_CONTENT

    override fun key(data: Uri) = data.toString()

    override suspend fun fetch(
        pool: BitmapPool,
        data: Uri,
        size: Size,
        options: Options
    ): FetchResult {
        val inputStream = if (isContactPhotoUri(data)) {
            // Modified from ContactsContract.Contacts.openContactPhotoInputStream.
            val stream: InputStream? =
                context.contentResolver.openAssetFileDescriptor(data, "r")?.createInputStream()
            checkNotNull(stream) { "Unable to find a contact photo associated with '$data'." }
        } else {
            val stream: InputStream? = context.contentResolver.openInputStream(data)
            checkNotNull(stream) { "Unable to open '$data'." }
        }

        return SourceResult(
            source = inputStream.source().buffer(),
            mimeType = context.contentResolver.getType(data),
            dataSource = DataSource.DISK
        )
    }

    /** Contact photos are a special case of content uris that must be loaded using [ContentResolver.openAssetFileDescriptor]. */
    @VisibleForTesting
    internal fun isContactPhotoUri(data: Uri): Boolean {
        return data.authority == ContactsContract.AUTHORITY && data.lastPathSegment == ContactsContract.Contacts.Photo.DISPLAY_PHOTO
    }
}
