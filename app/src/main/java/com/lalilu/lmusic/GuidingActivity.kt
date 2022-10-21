package com.lalilu.lmusic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import com.funny.data_saver.core.DataSaverInterface
import com.funny.data_saver.core.LocalDataSaver
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.lmusic.compose.screen.guiding.GuidingScreen
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuidingActivity : AppCompatActivity() {

    @Inject
    lateinit var dataSaver: DataSaverInterface

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
                    LocalDataSaver provides dataSaver,
                    LocalWindowSize provides calculateWindowSizeClass(activity = this),
                    LocalNavigatorHost provides navHostController
                ) {
                    GuidingScreen()
                }
            }
        }
    }
}