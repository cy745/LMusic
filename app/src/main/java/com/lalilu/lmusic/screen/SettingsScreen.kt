package com.lalilu.lmusic.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.RomUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SettingCategory
import com.lalilu.lmusic.screen.component.SettingStateSeekBar
import com.lalilu.lmusic.screen.component.SettingSwitcher

@Composable
fun SettingsScreen(
    contentPaddingForFooter: Dp = 0.dp
) {
    val ignoreAudioFocus = rememberDataSaverState("KEY_SETTINGS_ignore_audio_focus", false)
    val ablyServiceEnable = rememberDataSaverState("KEY_SETTINGS_ably_service_enable", false)
    val unknownFilter = rememberDataSaverState("KEY_SETTINGS_ably_unknown_filter", true)
    val statusBarLyric = rememberDataSaverState("KEY_SETTINGS_status_bar_lyric", false)
    val seekbarHandler = rememberDataSaverState("KEY_SETTINGS_seekbar_handler", 0)
    val lyricGravity = rememberDataSaverState("KEY_SETTINGS_lyric_gravity", 0)

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
                    SettingStateSeekBar(
                        state = seekbarHandler,
                        selection = listOf("单击", "双击", "长按"),
                        titleRes = R.string.preference_player_settings_seekbar_handler
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
                    if (RomUtils.isMeizu()) {
                        SettingSwitcher(
                            titleRes = R.string.preference_lyric_settings_status_bar_lyric,
                            state = statusBarLyric
                        )
                    }
                    SettingStateSeekBar(
                        state = lyricGravity,
                        selection = listOf("靠左", "居中", "靠右"),
                        titleRes = R.string.preference_lyric_settings_text_gravity
                    )
                }
            }
        }
    }
}