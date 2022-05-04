package com.lalilu.lmusic.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.RomUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.screen.component.*

@Composable
fun SettingsScreen(
    contentPaddingForFooter: Dp = 0.dp
) {
    val ignoreAudioFocus = rememberDataSaverState(Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS, false)
    val unknownFilter = rememberDataSaverState(Config.KEY_SETTINGS_MEDIA_UNKNOWN_FILTER, true)
    val statusBarLyric = rememberDataSaverState(Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE, false)
    val seekbarHandler = rememberDataSaverState(Config.KEY_SETTINGS_SEEKBAR_HANDLER, 0)
    val lyricGravity = rememberDataSaverState(Config.KEY_SETTINGS_LYRIC_GRAVITY, 0)
    val kanhiraEnable = rememberDataSaverState(Config.KEY_SETTINGS_KANHIRA_ENABLE, false)
    val repeatMode = rememberDataSaverState(Config.KEY_SETTINGS_REPEAT_MODE, 0)

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
                    SettingStateSeekBar(
                        state = repeatMode,
                        selection = listOf("列表循环", "单曲循环", "随机播放"),
                        title = "循环模式"
                    )
                    SettingStateSeekBar(
                        state = seekbarHandler,
                        selection = stringArrayResource(id = R.array.seekbar_handler).toList(),
                        titleRes = R.string.preference_player_settings_seekbar_handler
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
                        selection = stringArrayResource(id = R.array.lyric_gravity_text).toList(),
                        titleRes = R.string.preference_lyric_settings_text_gravity
                    )
                }
            }

            item {
                SettingCategory(
                    iconRes = R.drawable.ic_scan_line,
                    titleRes = R.string.preference_media_source_settings
                ) {
                    SettingSwitcher(
                        state = unknownFilter,
                        titleRes = R.string.preference_media_source_settings_unknown_filter,
                        subTitleRes = R.string.preference_media_source_tips
                    )
                }
            }

            item {
//                val isKanhiraInitialed by SearchTextUtil.isKanhiraInitialed
//                    .collectAsState(initial = false)

                SettingCategory(
                    iconRes = R.drawable.ic_lrc_fill,
                    titleRes = R.string.preference_lyric_settings
                ) {
                    SettingExtensionSwitcher(
                        state = kanhiraEnable,
                        initialed = true,
                        title = "罗马字匹配功能"
                    )
                }
            }
        }
    }
}