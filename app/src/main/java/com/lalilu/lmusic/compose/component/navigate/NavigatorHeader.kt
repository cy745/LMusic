package com.lalilu.lmusic.compose.component.navigate

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigatorHeader(
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int,
    extraContent: @Composable RowScope.() -> Unit = {}
) = NavigatorHeader(
    title = stringResource(id = titleRes),
    subTitle = stringResource(id = subTitleRes),
    extraContent = extraContent
)

@Composable
fun NavigatorHeader(
    title: String,
    subTitle: String,
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    NavigatorHeader(title = title, columnExtraContent = {
        Text(
            text = subTitle,
            fontSize = 14.sp,
            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                .copy(alpha = 0.5f)
        )
    }, rowExtraContent = extraContent)
}

@Composable
fun NavigatorHeader(
    title: String,
    columnExtraContent: @Composable ColumnScope.() -> Unit = {},
    rowExtraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(top = 26.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
            )
            columnExtraContent()
        }
        rowExtraContent()
    }
}
