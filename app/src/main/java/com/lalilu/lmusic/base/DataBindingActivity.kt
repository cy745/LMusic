package com.lalilu.lmusic.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class DataBindingActivity : AppCompatActivity() {
    var mBinding: ViewDataBinding? = null

    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = getDataBindingConfig()
        val binding: ViewDataBinding = DataBindingUtil.setContentView(this, config.getLayout())
        binding.lifecycleOwner = this
        config.getVmId()?.let {
            binding.setVariable(it, config.getStateVm())
        }
        config.getBindingParams().forEach { key, value ->
            binding.setVariable(key, value)
        }
        mBinding = binding
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding!!.unbind()
        mBinding = null
    }
}