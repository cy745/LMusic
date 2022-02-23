package com.lalilu.lmusic.fragment.viewmodel

import androidx.lifecycle.ViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorViewModel @Inject constructor() : ViewModel() {
    var singleUseFlag: Boolean = false
}