package com.lalilu.lmusic.screen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalilu.R
import com.lalilu.lmusic.screen.MainScreenData

@Composable
fun NavigatorFooter(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    popUp: () -> Unit = {},
    close: () -> Unit = {}
) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val previousScreenTitle = remember(currentBackStackEntry) {
        MainScreenData.fromRoute(
            navController.previousBackStackEntry?.destination?.route
        )?.title ?: R.string.dialog_bottom_sheet_navigator_back
    }.let { stringResource(id = it) }

    Row(
        modifier = modifier
            .clickable(enabled = false) {}
            .background(color = MaterialTheme.colors.background.copy(alpha = 0.9f))
            .navigationBarsPadding()
            .height(52.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        TextButton(
            contentPadding = PaddingValues(horizontal = 10.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = contentColor
            ),
            onClick = popUp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_left_s_line),
                contentDescription = "backButtonIcon",
                colorFilter = ColorFilter.tint(color = contentColor)
            )
            Text(
                text = previousScreenTitle,
                fontSize = 14.sp
            )
        }
        TextButton(
            contentPadding = PaddingValues(horizontal = 20.dp),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = Color(0x25FE4141),
                contentColor = Color(0xFFFE4141)
            ),
            onClick = close
        ) {
            Text(
                text = stringResource(id = R.string.dialog_bottom_sheet_navigator_close),
                fontSize = 14.sp
            )
        }
    }
}