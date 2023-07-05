package com.lalilu.lmusic.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lalilu.lmusic.LMusicTheme
import com.lalilu.lmusic.compose.component.DynamicTips
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.new_screen.LMusicNavHost
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.edgeTransparentForStatusBar

@OptIn(
    ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class
)
class LibraryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.apply {
            setContent {
                LMusicTheme {
                    CompositionLocalProvider(
                        LocalWindowSize provides calculateWindowSizeClass(activity = this@LibraryFragment.requireActivity()),
                        LocalNavigatorHost provides rememberAnimatedNavController(),
                    ) {
                        Box {
                            LMusicNavHost(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .edgeTransparentForStatusBar(SmartModalBottomSheet.enableFadeEdgeForStatusBar.value)
                            )
                            DynamicTips.Content(modifier = Modifier.align(Alignment.TopCenter))
                        }
                    }
                }
            }
        }
    }
}