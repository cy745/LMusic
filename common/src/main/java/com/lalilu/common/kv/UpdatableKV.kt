package com.lalilu.common.kv

import kotlinx.coroutines.flow.Flow

interface UpdatableKV<T> {
    fun flow(): Flow<T?>
    fun get(): T?
    fun set(value: T?)
    fun save()
    fun update()
    fun enableAutoSave()
    fun disableAutoSave()
}