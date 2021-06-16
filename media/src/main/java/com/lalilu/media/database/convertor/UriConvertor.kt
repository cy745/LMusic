package com.lalilu.media.database.convertor

import android.net.Uri
import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON

class UriConvertor {
    @TypeConverter
    fun revert(value: String?): Uri? {
        return JSON.parseObject(value, Uri::class.java)
    }

    @TypeConverter
    fun converter(value: Uri?): String? {
        return JSON.toJSONString(value)
    }
}