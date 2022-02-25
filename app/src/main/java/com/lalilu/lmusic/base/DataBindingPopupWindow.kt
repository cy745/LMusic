package com.lalilu.lmusic.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

@SuppressLint("ClickableViewAccessibility")
abstract class DataBindingPopupWindow(
    private val context: Context,
) : PopupWindow(context) {
    init {
        onCreateView()
    }

    abstract fun getDataBindingConfig(): DataBindingConfig
    abstract fun getTargetView(bd: ViewDataBinding): View
    open fun onViewCreated(bd: ViewDataBinding) {}
    open fun onDismiss(cancel: Boolean) {}

    fun dismiss(cancel: Boolean) {
        onDismiss(cancel)
        dismiss()
    }

    private fun onCreateView() {
        val config = getDataBindingConfig()
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), config.getLayout(),
            null, false
        )
        config.getVmId()?.let {
            binding.setVariable(it, config.getStateVm())
        }
        config.getBindingParams().forEach { key, value ->
            binding.setVariable(key, value)
        }
        contentView = binding.root
        onViewCreated(binding)
        binding.root.setOnTouchListener { _, motionEvent ->
            val targetView = getTargetView(binding)
            val xLimit = targetView.x
            val yLimit = targetView.y
            val widthLimit = targetView.width
            val heightLimit = targetView.height
            val isInside = motionEvent.x in xLimit..(xLimit + widthLimit)
                    && motionEvent.y in yLimit..(yLimit + heightLimit)
            if (!isInside) dismiss(true)
            return@setOnTouchListener isInside
        }
    }
}