package com.lalilu.lmusic.utils.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.lmusic.entity.Artist

class ArtistConvertor {
    @TypeConverter
    fun revert(value: String?): Artist? {
        return JSON.parseObject(value, Artist::class.java)
    }

    @TypeConverter
    fun converter(value: Artist?): String? {
        return JSON.toJSONString(value)
    }
}