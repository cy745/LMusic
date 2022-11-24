package com.lalilu.lmusic.compose.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.viewmodel.LocalMainVM
import com.lalilu.lmusic.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TestUDPScreen(
    mainVM: MainViewModel = LocalMainVM.current
) {
    DisposableEffect(Unit) {
        onDispose {
            mainVM.stopListen()
        }
    }

    SmartContainer.LazyColumn {
        item {
            Button(onClick = { if (!mainVM.searching.value) mainVM.search2() }) {
                AnimatedContent(targetState = mainVM.searching.value) {
                    if (it) {
                        Text(text = "Searching...")
                    } else {
                        Text(text = "Search")
                    }
                }
            }
        }
        items(items = mainVM.remoteDeviceList, key = { it }) {
            Text(text = it)
        }
    }
}