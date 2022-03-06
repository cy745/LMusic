package com.lalilu.lmusic.viewmodel

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.adapter.BaseAdapter

fun <I : Any, B : ViewDataBinding> BaseAdapter<I, B>.bindViewModel(
    viewModel: BaseViewModel<I>,
    lifecycleOwner: LifecycleOwner
) {
    this.stateRestorationPolicy =
        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    viewModel.recyclerViewData.observe(lifecycleOwner) {
        this.setDiffNewData(it.toMutableList())
    }
    viewModel.recyclerViewPosition.observe(lifecycleOwner) {
        this.mRecyclerView?.get()?.scrollToPosition(it)
    }
}

fun <I : Any, B : ViewDataBinding> BaseAdapter<I, B>.savePosition(
    viewModel: BaseViewModel<I>
) {
    val position = this.mRecyclerView?.get()?.computeVerticalScrollOffset() ?: 0
    viewModel.updatePosition(position)
}

/**
 * 具有记忆[RecyclerView]的[recyclerViewData]和[recyclerViewPosition]功能的基础[ViewModel]
 */
open class BaseViewModel<DATA> : ViewModel() {
    val recyclerViewData: MutableLiveData<List<DATA>> = MutableLiveData(emptyList())
    val recyclerViewPosition: MutableLiveData<Int> = MutableLiveData(0)

    fun updatePosition(position: Int) {
        recyclerViewPosition.postValue(position)
    }

    fun postData(data: List<DATA>) {
        recyclerViewData.postValue(data)
    }
}