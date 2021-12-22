package com.lalilu.lmusic.base

import android.util.SparseArray
import androidx.lifecycle.ViewModel

class DataBindingConfig(
    private var mLayoutId: Int,
    private var vmVariableId: Int? = null,
    private var stateViewModel: ViewModel? = null,
) {
    private var mBindingParams = SparseArray<Any>()

    fun getLayout() = mLayoutId
    fun getBindingParams() = mBindingParams
    fun getVmId() = vmVariableId
    fun getStateVm() = stateViewModel

    fun addParam(key: Int, value: Any?): DataBindingConfig {
        if (mBindingParams[key] == null) {
            mBindingParams.put(key, value)
        }
        return this
    }
}