package com.lalilu.lmusic.utils.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.lmusic.entity.Song

class SongConvertor {
    @TypeConverter
    fun revert(value: String?): Song? {
        return JSON.parseObject(value, Song::class.java)
    }

    @TypeConverter
    fun converter(value: Song?): String? {
        return JSON.toJSONString(value)
    }
}