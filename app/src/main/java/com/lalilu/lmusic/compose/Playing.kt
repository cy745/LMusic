package com.lalilu.lmusic.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.lalilu.databinding.FragmentPlayingContainerBinding

object Playing {

    @Composable
    fun Content() {
        AndroidViewBinding(
            modifier = Modifier.fillMaxSize(),
            factory = FragmentPlayingContainerBinding::inflate
        ) {
        }
    }
}

