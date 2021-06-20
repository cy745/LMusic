package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import java.util.*

class MediaIdTreeSetConvertor {
    @TypeConverter
    fun revert(value: String?): TreeSet<String> {
        return TreeSet(JSON.parseArray(value, String::class.java))
    }

    @TypeConverter
    fun converter(value: TreeSet<String>?): String? {
        return JSON.toJSONString(value)
    }
}