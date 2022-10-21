package com.lalilu.lmusic.compose.component.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingCategory(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    content: @Composable ColumnScope.() -> Unit = {}
) = SettingCategory(
    icon = painterResource(id = iconRes),
    title = stringResource(id = titleRes),
    content = content
)

@Composable
fun SettingCategory(
    icon: Painter,
    title: String,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
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
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
        Spacer(modifier = Modifier.height(30.dp))
    }
}
