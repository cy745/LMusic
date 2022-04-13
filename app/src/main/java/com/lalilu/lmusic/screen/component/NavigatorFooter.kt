package com.lalilu.lmusic.screen.component

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import com.lalilu.R
import com.lalilu.lmusic.screen.MainScreenData

val previousBackStackEntry: MutableState<NavBackStackEntry?> = mutableStateOf(null)
val onDestinationChangeListener = { controller: NavController, _: NavDestination, _: Bundle? ->
    previousBackStackEntry.value = controller.previousBackStackEntry
}

@Composable
fun NavigatorFooter(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    popUp: () -> Unit = {},
    close: () -> Unit = {}
) {
    navController.removeOnDestinationChangedListener(onDestinationChangeListener)
    navController.addOnDestinationChangedListener(onDestinationChangeListener)

    val previousScreenTitle = MainScreenData.fromRoute(
        previousBackStackEntry.value?.destination?.route
    )?.let { stringResource(id = it.title) }
        ?: stringResource(id = R.string.dialog_bottom_sheet_navigator_back)

    Column(modifier = modifier) {
        Divider(
            modifier = Modifier.padding(horizontal = 5.dp),
            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                .copy(0.2f)
        )
        Row(
            modifier = Modifier
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
}