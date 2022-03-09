package com.lalilu.lmusic.viewmodel

import androidx.lifecycle.ViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorViewModel @Inject constructor() : ViewModel() {
    var startFrom: Int = -1
}