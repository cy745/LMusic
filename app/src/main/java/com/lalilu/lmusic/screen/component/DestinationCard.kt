package com.lalilu.lmusic.screen.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmusic.screen.MainScreenData


@Composable
fun DestinationCard(
    route: MainScreenData,
    navigateTo: (String) -> Unit
) = DestinationCard(
    iconRes = route.icon,
    titleRes = route.title,
    onClick = { navigateTo(route.name) }
)

@Composable
fun DestinationCard(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    onClick: () -> Unit
) = DestinationCard(
    icon = painterResource(id = iconRes),
    title = stringResource(id = titleRes),
    onClick = onClick
)

@Composable
fun DestinationCard(
    icon: Painter,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        elevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .clickable(onClick = onClick)
                .padding(20.dp),
        ) {
            Image(
                painter = icon,
                contentDescription = ""
            )
            Text(text = title)
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
                contentDescription = ""
            )
        }
    }
}