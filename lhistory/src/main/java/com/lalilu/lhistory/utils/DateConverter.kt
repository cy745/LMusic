package com.lalilu.lhistory.utils

import android.net.Uri
import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun recover(value: Long): Date = Date(value)

    @TypeConverter
    fun convert(date: Date): Long = date.time
}

class UriConverter {
    @TypeConverter
    fun recover(value: String): Uri = Uri.parse(value)

    @TypeConverter
    fun convert(uri: Uri): String = uri.toString()
}
