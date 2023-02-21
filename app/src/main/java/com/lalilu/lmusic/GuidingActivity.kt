package com.lalilu.lmusic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.lmusic.compose.screen.guiding.GuidingScreen
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize

class GuidingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @OptIn(
            ExperimentalMaterial3WindowSizeClassApi::class,
            ExperimentalAnimationApi::class
        )
        setContent {
            LMusicTheme {
                val navHostController = rememberAnimatedNavController()

                CompositionLocalProvider(
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides navHostController
                ) {
                    GuidingScreen()
                }
            }
        }
    }
}