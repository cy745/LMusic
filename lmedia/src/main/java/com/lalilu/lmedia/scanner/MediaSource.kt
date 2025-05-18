package com.lalilu.lmedia.scanner

import kotlinx.coroutines.flow.Flow

interface MediaSource<T> {
    fun updateAsync()
    fun requireFlow(): Flow<List<T>>
}