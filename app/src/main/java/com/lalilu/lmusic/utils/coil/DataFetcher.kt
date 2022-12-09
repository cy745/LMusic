package com.lalilu.lmusic.utils.coil

import kotlinx.coroutines.flow.Flow

interface DataFetcher {
    fun fetch(id: Any?): Flow<Any?>
}