package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON

class MediaIdListConvertor {
    @TypeConverter
    fun revert(value: String?): MutableList<String>? {
        return JSON.parseArray(value, String::class.java)
    }

    @TypeConverter
    fun converter(value: MutableList<String>?): String? {
        return JSON.toJSONString(value)
    }
}