package com.lalilu.lmusic.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.LazyGridContent
import com.lalilu.component.base.LocalWindowSize
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.divider
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.rememberGridItemPadding
import com.zhangke.krouter.KRouter


object EntryPanel : LazyGridContent {

    @Composable
    override fun register(): LazyGridScope.() -> Unit {
        val screenEntry = remember {
            listOfNotNull<Screen>(
                KRouter.route("/pages/songs"),
                KRouter.route("/pages/artists"),
                KRouter.route("/pages/albums"),
                KRouter.route("/pages/playlist"),
                KRouter.route("/pages/history"),
                KRouter.route("/pages/folders"),
                KRouter.route("/pages/settings")
            )
        }
        val defaultString = "Undefined"
        val widthSizeClass = LocalWindowSize.current.widthSizeClass
        val gridItemPaddings = rememberGridItemPadding(
            count = if (widthSizeClass == WindowWidthSizeClass.Expanded) 3 else 2,
            gapVertical = 8.dp,
            gapHorizontal = 8.dp,
            paddingValues = PaddingValues(horizontal = 16.dp)
        )

        return fun LazyGridScope.() {
            divider { it.height(16.dp) }

            itemsIndexed(
                items = screenEntry,
                key = { index, item -> item.key },
                contentType = { index, item -> this@EntryPanel::class.java.name },
                span = { index, item ->
                    if (widthSizeClass == WindowWidthSizeClass.Expanded) {
                        GridItemSpan(maxLineSpan / 3)
                    } else {
                        GridItemSpan(maxLineSpan / 2)
                    }
                }
            ) { index, item ->
                val infoFactory = (item as? ScreenInfoFactory)?.provideScreenInfo()
                val title = infoFactory?.title?.invoke() ?: defaultString
                val icon = infoFactory?.icon

                Surface(
                    modifier = Modifier.padding(gridItemPaddings(index)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { AppRouter.intent(NavIntent.Push(item)) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = MaterialTheme.colors.onBackground.copy(0.7f)
                            )
                        }

                        Text(
                            text = title,
                            color = MaterialTheme.colors.onBackground.copy(0.6f),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }

            divider()
        }
    }
}
