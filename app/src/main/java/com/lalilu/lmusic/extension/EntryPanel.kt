package com.lalilu.lmusic.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lalbum.screen.AlbumsScreen
import com.lalilu.lartist.screen.ArtistsScreen
import com.lalilu.ldictionary.screen.DictionaryScreen
import com.lalilu.lhistory.screen.HistoryScreen
import com.lalilu.lmusic.compose.new_screen.SettingsScreen
import com.lalilu.lmusic.compose.new_screen.SongsScreen
import com.lalilu.lplaylist.screen.PlaylistScreen

@Composable
fun EntryPanel() {
    val screenEntry = remember {
        listOf(
            SongsScreen(),
            ArtistsScreen(),
            AlbumsScreen(),
            PlaylistScreen,
            HistoryScreen,
            DictionaryScreen,
            SettingsScreen
        )
    }

    Surface(
        modifier = Modifier.padding(15.dp),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column {
            for (entry in screenEntry) {
                val (icon, title) = when (entry) {
                    is DynamicScreen -> entry.getScreenInfo()?.let { it.icon to it.title }
                    is ScreenInfoFactory -> entry.provideScreenInfo().let { it.icon to it.title }
                    else -> null
                } ?: continue

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            AppRouter.intent(
                                NavIntent.Push(entry)
                            )
                        }
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    icon?.let { icon ->
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = stringResource(id = title),
                            tint = dayNightTextColor(0.7f)
                        )
                    }

                    Text(
                        text = stringResource(id = title),
                        color = dayNightTextColor(0.6f),
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
    }
}
