package com.lalilu.component.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingCategory(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    content: @Composable ColumnScope.() -> Unit = {}
) = SettingCategory(
    modifier = modifier,
    icon = painterResource(id = iconRes),
    title = stringResource(id = titleRes),
    content = content
)

@Composable
fun SettingCategory(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    gapHeight: Dp = 10.dp,
    contentEnd: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(gapHeight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val color = contentColorFor(
                backgroundColor = MaterialTheme.colors.background
            ).copy(0.7f)
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon,
                contentDescription = title,
                tint = color
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = color
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            contentEnd()
        }
        content()
    }
}
