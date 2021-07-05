package com.lalilu.lmusic.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class BaseRequest<T> {
    protected val data = MutableLiveData<T>()
    open fun requestData() {}
    open fun requestData(value: Any?) {}

    fun postData(newData: T?) {
        data.postValue(newData)
    }

    fun setData(newData: T?) {
        data.value = newData
    }

    fun getData(): LiveData<T> = data
}