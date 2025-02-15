package com.lalilu.lmusic.compose

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
import com.funny.data_saver.core.LocalDataSaver
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.lmusic.LMusicTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
object App {

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    fun Content(activity: Activity) {
        Environment(activity = activity) {
            Box(modifier = Modifier.fillMaxSize()) {
                ProvideNavigatorLifecycleKMPSupport {
                    LayoutWrapperContent()
                }
            }
        }
    }

    @Composable
    fun Environment(activity: Activity, content: @Composable () -> Unit) {
        LMusicTheme {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalWindowSize provides calculateWindowSizeClass(activity = activity),
                    LocalDataSaver provides koinInject(),
                    content = content
                )
            }
        }
    }
}