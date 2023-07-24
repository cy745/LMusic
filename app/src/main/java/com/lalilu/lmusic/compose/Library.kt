package com.lalilu.lmusic.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.lalilu.lmusic.compose.component.SmartBar.SmartBarContent

object Library {
    @Composable
    fun Content() {
        Box {
            NavigationWrapper.Content()
            SmartBarContent()
        }
    }
}