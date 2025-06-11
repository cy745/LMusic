package com.lalilu.lmedia.repository

import com.lalilu.common.kv.KVContext

object LMediaKV : KVContext("lmedia") {
    val includePath = obtainList<String>("INCLUDE_PATH")
}