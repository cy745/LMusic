package com.lalilu.lmusic.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseFragment : DataBindingFragment() {
    private var mFragmentProvider: ViewModelProvider? = null
    private var mActivityProvider: ViewModelProvider? = null
    private var mApplicationProvider: ViewModelProvider? = null

    open fun loadInitData() {}
    open fun loadInitView() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInitData()
        loadInitView()
    }

    protected fun <T : ViewModel?> getFragmentViewModel(modelClass: Class<T>): T {
        if (mFragmentProvider == null) {
            mFragmentProvider = ViewModelProvider(this)
        }
        return mFragmentProvider!![modelClass]
    }

    protected fun <T : ViewModel?> getActivityViewModel(modelClass: Class<T>): T {
        if (mActivityProvider == null) {
            mActivityProvider = ViewModelProvider(this)
        }
        return mActivityProvider!![modelClass]
    }

    protected fun <T : ViewModel?> getApplicationViewModel(modelClass: Class<T>): T {
        if (mApplicationProvider == null) {
            mApplicationProvider =
                ViewModelProvider(
                    mActivity!!.applicationContext as BaseApplication,
                    getAppFactory(mActivity!!)
                )
        }
        return mApplicationProvider!![modelClass]
    }

    private fun getAppFactory(activity: Activity): ViewModelProvider.Factory {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
    }
}