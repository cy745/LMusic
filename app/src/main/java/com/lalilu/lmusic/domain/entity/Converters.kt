package com.lalilu.lmusic.domain.entity

import android.net.Uri
import androidx.room.TypeConverter

class UriConverter {
    @TypeConverter
    fun toString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(text: String): Uri {
        return Uri.parse(text)
    }
}

