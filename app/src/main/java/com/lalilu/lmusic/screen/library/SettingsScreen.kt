package com.lalilu.lmusic.screen.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RomUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.R
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.GuidingActivity
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.NavigatorHeader
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.settings.*
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.getActivity

@Composable
fun SettingsScreen() {
    val contentPaddingForFooter by SmartBar.contentPaddingForSmartBarDp
    val windowSize = LocalWindowSize.current
    val context = LocalContext.current
    val ignoreAudioFocus = rememberDataSaverState(
        Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS,
        Config.DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS
    )
    val unknownFilter = rememberDataSaverState(
        Config.KEY_SETTINGS_MEDIA_UNKNOWN_FILTER,
        Config.DEFAULT_SETTINGS_MEDIA_UNKNOWN_FILTER
    )
    val statusBarLyric = rememberDataSaverState(
        Config.KEY_SETTINGS_STATUS_LYRIC_ENABLE,
        Config.DEFAULT_SETTINGS_STATUS_LYRIC_ENABLE
    )
    val seekbarHandler = rememberDataSaverState(
        Config.KEY_SETTINGS_SEEKBAR_HANDLER,
        Config.DEFAULT_SETTINGS_SEEKBAR_HANDLER
    )
    val lyricGravity = rememberDataSaverState(
        Config.KEY_SETTINGS_LYRIC_GRAVITY,
        Config.DEFAULT_SETTINGS_LYRIC_GRAVITY
    )
    val lyricTextSize = rememberDataSaverState(
        Config.KEY_SETTINGS_LYRIC_TEXT_SIZE,
        Config.DEFAULT_SETTINGS_LYRIC_TEXT_SIZE
    )
    val kanhiraEnable = rememberDataSaverState(
        Config.KEY_SETTINGS_KANHIRA_ENABLE,
        Config.DEFAULT_SETTINGS_KANHIRA_ENABLE
    )
    val repeatMode = rememberDataSaverState(
        Config.KEY_SETTINGS_REPEAT_MODE,
        Config.DEFAULT_SETTINGS_REPEAT_MODE
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavigatorHeader(route = MainScreenData.Settings)

        LazyVerticalGrid(
            columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
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
                    SettingProgressSeekBar(
                        state = lyricTextSize,
                        title = "歌词文字大小",
                        valueRange = 12..26
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
                    iconRes = R.drawable.ic_gradienter_line,
                    titleRes = R.string.preference_extensions
                ) {
                    SettingExtensionSwitcher(
                        state = kanhiraEnable,
                        initialed = true,
                        title = "罗马字匹配功能"
                    )
                }
            }

            item {
                SettingCategory(
                    icon = painterResource(id = R.drawable.ic_loader_line),
                    title = "其他"
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        TextButton(onClick = {
                            context.getActivity()?.apply {
                                ActivityUtils.startActivity(GuidingActivity::class.java)
                            }
                        }) {
                            Text(text = "新手引导")
                        }
                    }
                }
            }
        }
    }
}