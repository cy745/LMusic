package com.lalilu.lmusic.datastore

import com.lalilu.common.kv.KVContext

object TempKV : KVContext("temp") {
    val dayOfYear = obtain<Int>("DAY_OF_YEAR")
    val dailyRecommends = obtainList<String>("DAILY_RECOMMENDS")
}