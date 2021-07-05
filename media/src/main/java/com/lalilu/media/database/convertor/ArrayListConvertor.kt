package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON

class ArrayListConvertor {
    @TypeConverter
    fun revert(value: String?): ArrayList<Long> {
        return ArrayList(JSON.parseArray(value, Long::class.java))
    }

    @TypeConverter
    fun converter(value: ArrayList<Long>): String? {
        return JSON.toJSONString(value)
    }
}