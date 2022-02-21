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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class DataBindingBottomSheetDialogFragment : BottomSheetDialogFragment() {
    protected var mActivity: AppCompatActivity? = null
    protected var mBinding: ViewDataBinding? = null

    open fun onCreate() {}
    open fun onViewCreated() {}
    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val config = getDataBindingConfig()
        val binding: ViewDataBinding =
            DataBindingUtil.inflate(inflater, config.getLayout(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        config.getVmId()?.let {
            binding.setVariable(it, config.getStateVm())
        }
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