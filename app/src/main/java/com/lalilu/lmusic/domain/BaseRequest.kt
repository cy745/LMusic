package com.lalilu.lmusic.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Request抽象类，提供更新的方法，不能直接修改
 */
@Deprecated("使用Flow构建单向数据流，不需要Request了")
abstract class BaseRequest<T> {
    protected val data = MutableLiveData<T>()
    private var lastRequest: Any? = null

    open fun requireData() {
        requireData(lastRequest)
    }

    open fun requestData() {
        requestData(lastRequest)
    }

    open fun requestData(value: Any?) {
        lastRequest = value
    }

    open fun requireData(value: Any?) {
        lastRequest = value
    }

    fun postData(newData: T?) {
        data.postValue(newData)
    }

    fun setData(newData: T?) {
        data.value = newData
    }

    fun getData(): LiveData<T> = data
}