package com.lalilu.lmusic.screen.component.card

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
import com.lalilu.lmusic.screen.ScreenData


@Composable
fun DestinationCard(
    route: ScreenData,
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
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 20.dp, horizontal = 15.dp),
        ) {
            Image(
                painter = icon,
                contentDescription = "destination_icon"
            )
            Text(text = title)
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
                contentDescription = ""
            )
        }
    }
}