package com.lalilu.lmusic.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class DataBindingActivity : AppCompatActivity() {
    var mBinding: ViewDataBinding? = null

    abstract fun initViewModel()
    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()

        val config = getDataBindingConfig()
        val binding: ViewDataBinding = DataBindingUtil.setContentView(this, config.getLayout())
        binding.lifecycleOwner = this
        binding.setVariable(config.getVmId(), config.getStateVm())
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