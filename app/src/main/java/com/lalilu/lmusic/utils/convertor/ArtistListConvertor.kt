package com.lalilu.lmusic.utils.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.lmusic.entity.Artist

class ArtistListConvertor {
    @TypeConverter
    fun revert(value: String?): List<Artist>? {
        return JSON.parseArray(value).toJavaList(Artist::class.java)
    }

    @TypeConverter
    fun converter(value: List<Artist>?): String? {
        return JSON.toJSONString(value)
    }
}