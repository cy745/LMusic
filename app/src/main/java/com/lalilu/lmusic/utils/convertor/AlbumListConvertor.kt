package com.lalilu.lmusic.utils.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.lmusic.entity.Album

class AlbumListConvertor {
    @TypeConverter
    fun revert(value: String?): List<Album>? {
        return JSON.parseArray(value).toJavaList(Album::class.java)
    }

    @TypeConverter
    fun converter(value: List<Album>?): String? {
        return JSON.toJSONString(value)
    }
}