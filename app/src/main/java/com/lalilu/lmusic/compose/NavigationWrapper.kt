package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lalilu.lmusic.compose.component.BottomSheetNavigator
import com.lalilu.lmusic.compose.component.CustomTransition
import com.lalilu.lmusic.compose.new_screen.HomeScreen


@OptIn(ExperimentalMaterialApi::class)
object NavigationWrapper {
    var navigator: BottomSheetNavigator? = null
        private set

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        BottomSheetNavigator(
            defaultScreen = HomeScreen,
            ignoreFlingNestedScroll = true,
            modifier = modifier.fillMaxSize(),
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetBackgroundColor = MaterialTheme.colors.background,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1000f
            ),
            sheetContent = { bottomSheetNavigator ->
                navigator = bottomSheetNavigator
                Box {
                    CustomTransition(navigator = bottomSheetNavigator.getNavigator()) {
                        it.Content()
                    }

                    val item = bottomSheetNavigator.lastItemOrNull ?: return@Box
                    (item as? CustomScreen)?.getExtraContent()?.invoke()
                }
            },
            content = { Playing.Content() }
        )
    }
}