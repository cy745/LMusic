package com.lalilu.lmusic.viewmodel

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.lalilu.lmusic.adapter.BaseAdapter

fun <I : Any, B : ViewDataBinding> BaseAdapter<I, B>.bindViewModel(
    viewModel: BaseViewModel<List<I>>,
    lifecycleOwner: LifecycleOwner,
    scrollToTopWhenRefresh: Boolean = false
) {
    this.stateRestorationPolicy =
        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    viewModel.recyclerViewData.observe(lifecycleOwner) {
        this.setDiffNewData(it.toMutableList())
        if (scrollToTopWhenRefresh) {
            this.mRecyclerView?.get()?.scrollToPosition(0)
        }
    }
    viewModel.recyclerViewPosition.observe(lifecycleOwner) {
        this.mRecyclerView?.get()?.scrollToPosition(it)
    }
}

fun <I : Any, B : ViewDataBinding> BaseAdapter<I, B>.savePosition(
    viewModel: BaseViewModel<List<I>>
) {
    val position = this.mRecyclerView?.get()?.computeVerticalScrollOffset() ?: 0
    viewModel.updatePosition(position)
}

/**
 * 具有记忆[RecyclerView]的[recyclerViewData]和[recyclerViewPosition]功能的基础[ViewModel]
 */
open class BaseViewModel<DATA : List<Any>> : ViewModel() {
    val recyclerViewData: MutableLiveData<DATA> = MutableLiveData()
    val recyclerViewPosition: MutableLiveData<Int> = MutableLiveData(0)

    fun updatePosition(position: Int) {
        recyclerViewPosition.postValue(position)
    }

    fun postData(data: DATA) {
        recyclerViewData.postValue(data)
    }
}