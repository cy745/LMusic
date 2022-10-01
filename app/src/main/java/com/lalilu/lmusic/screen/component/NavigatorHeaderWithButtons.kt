package com.lalilu.lmusic.screen.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.screen.ScreenData

@Composable
fun NavigatorHeaderWithButtons(
    route: ScreenData,
    buttonsContent: @Composable RowScope.() -> Unit = {}
) = NavigatorHeaderWithButtons(
    titleRes = route.title,
    subTitleRes = route.subTitle,
    buttonsContent = buttonsContent
)

@Composable
fun NavigatorHeaderWithButtons(
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int,
    buttonsContent: @Composable RowScope.() -> Unit = {}
) = NavigatorHeaderWithButtons(
    title = stringResource(id = titleRes),
    subTitle = stringResource(id = subTitleRes),
    buttonsContent = buttonsContent
)

@Composable
fun NavigatorHeaderWithButtons(
    title: String,
    subTitle: String,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    buttonsContent: @Composable RowScope.() -> Unit = {}
) {
    NavigatorHeader(title = title, subTitle = subTitle) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(50.dp))
                .background(color = if (isDarkMode) Color.Black else Color.White)
                .padding(vertical = 5.dp, horizontal = 10.dp)
        ) {
            buttonsContent()
        }
    }
}