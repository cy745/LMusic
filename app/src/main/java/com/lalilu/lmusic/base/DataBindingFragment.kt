package com.lalilu.lmusic.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class DataBindingFragment : Fragment() {
    protected var mActivity: AppCompatActivity? = null
    protected var mBinding: ViewDataBinding? = null

    abstract fun initViewModel()
    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val config = getDataBindingConfig()
        val binding: ViewDataBinding =
            DataBindingUtil.inflate(inflater, config.getLayout(), container, false)
        binding.lifecycleOwner = this
        binding.setVariable(config.getVmId(), config.getStateVm())
        config.getBindingParams().forEach { key, value ->
            binding.setVariable(key, value)
        }
        mBinding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.unbind()
        mBinding = null
    }
}