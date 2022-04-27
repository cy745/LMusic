package com.lalilu.lmusic.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigatorHeader

@Composable
fun SettingsScreen(
    contentPaddingForFooter: Dp = 0.dp
) {
    val ignoreAudioFocus = rememberDataSaverState("KEY_SETTINGS_ignore_audio_focus", false)
    val ablyServiceEnable = rememberDataSaverState("KEY_SETTINGS_ably_service_enable", false)
    val unknownFilter = rememberDataSaverState("KEY_SETTINGS_ably_unknown_filter", true)
    val statusBarLyric = rememberDataSaverState("KEY_SETTINGS_status_bar_lyric", false)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavigatorHeader(route = MainScreenData.Settings)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = contentPaddingForFooter)
        ) {
            item {
                SettingCategory(
                    iconRes = R.drawable.ic_settings_4_line,
                    titleRes = R.string.preference_player_settings
                ) {
                    SettingSwitcher(
                        titleRes = R.string.preference_player_settings_ignore_audio_focus,
                        state = ignoreAudioFocus
                    )
                    SettingSwitcher(
                        titleRes = R.string.preference_ably_service_enable,
                        subTitleRes = R.string.preference_lyric_experimental,
                        state = ablyServiceEnable
                    )
                }
            }

            item {
                SettingCategory(
                    iconRes = R.drawable.ic_scan_line,
                    titleRes = R.string.preference_media_source_settings
                ) {
                    SettingSwitcher(
                        titleRes = R.string.preference_media_source_settings_unknown_filter,
                        state = unknownFilter
                    )
                }
            }

            item {
                SettingCategory(
                    iconRes = R.drawable.ic_lrc_fill,
                    titleRes = R.string.preference_lyric_settings
                ) {
                    SettingSwitcher(
                        titleRes = R.string.preference_lyric_settings_status_bar_lyric,
                        state = statusBarLyric
                    )
                }
            }
        }
    }
}

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
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SettingSwitcher(
    state: DataSaverMutableState<Boolean>,
    @StringRes titleRes: Int,
    @StringRes subTitleRes: Int? = null
) = SettingSwitcher(
    state = state,
    title = stringResource(id = titleRes),
    subTitle = subTitleRes?.let { stringResource(id = it) }
)

@Composable
fun SettingSwitcher(
    state: DataSaverMutableState<Boolean>,
    title: String,
    subTitle: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple()
    var value by state
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = { value = !value }
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = title)
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = contentColorFor(
                        backgroundColor = MaterialTheme.colors.background
                    ).copy(0.5f)
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = { value = it },
            interactionSource = interactionSource
        )
    }
}