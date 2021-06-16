package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.media.entity.LMusicMediaItem

class SongConvertor {
    @TypeConverter
    fun revert(value: String?): LMusicMediaItem? {
        return JSON.parseObject(value, LMusicMediaItem::class.java)
    }

    @TypeConverter
    fun converter(value: LMusicMediaItem?): String? {
        return JSON.toJSONString(value)
    }
}