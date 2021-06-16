package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.media.entity.LMusicAlbum

class AlbumConvertor {
    @TypeConverter
    fun revert(value: String?): LMusicAlbum? {
        return JSON.parseObject(value, LMusicAlbum::class.java)
    }

    @TypeConverter
    fun converter(value: LMusicAlbum?): String? {
        return JSON.toJSONString(value)
    }
}