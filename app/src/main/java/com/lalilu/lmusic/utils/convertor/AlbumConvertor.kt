package com.lalilu.lmusic.utils.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.lmusic.entity.Album

class AlbumConvertor {
    @TypeConverter
    fun revert(value: String?): Album? {
        return JSON.parseObject(value, Album::class.java)
    }

    @TypeConverter
    fun converter(value: Album?): String? {
        return JSON.toJSONString(value)
    }
}