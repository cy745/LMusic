package com.lalilu.common.kv

import kotlinx.coroutines.flow.Flow

interface UpdatableKV<T> {
    fun flow(): Flow<T?>
    fun getData(): T
    fun setData(value: T)

    fun remove()
    fun save()
    fun update()
    fun enableAutoSave()
    fun disableAutoSave()
}