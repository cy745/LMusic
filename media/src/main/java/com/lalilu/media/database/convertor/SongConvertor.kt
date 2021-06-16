package com.lalilu.media.database.convertor

import androidx.room.TypeConverter
import com.alibaba.fastjson.JSON
import com.lalilu.media.entity.LMusicMedia

class SongConvertor {
    @TypeConverter
    fun revert(value: String?): LMusicMedia? {
        return JSON.parseObject(value, LMusicMedia::class.java)
    }

    @TypeConverter
    fun converter(value: LMusicMedia?): String? {
        return JSON.toJSONString(value)
    }
}